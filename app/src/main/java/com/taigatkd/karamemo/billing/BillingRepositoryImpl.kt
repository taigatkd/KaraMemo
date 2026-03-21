package com.taigatkd.karamemo.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.taigatkd.karamemo.BuildConfig
import com.taigatkd.karamemo.data.repository.PreferencesRepository
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BillingRepositoryImpl(
    context: Context,
    private val preferencesRepository: PreferencesRepository,
) : BillingRepository {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
    private val _state = MutableStateFlow(
        BillingState(
            canUseMockBilling = BuildConfig.MOCK_BILLING_ENABLED,
        ),
    )
    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 8)
    private var productDetails: ProductDetails? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            scope.launch {
                _state.update { it.copy(isPurchaseInProgress = false) }

                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        val matchingPurchases = purchases.orEmpty().filter(::isProPurchase)
                        if (matchingPurchases.isEmpty()) {
                            preferencesRepository.setCachedRealProEnabled(false)
                            return@launch
                        }

                        val granted = matchingPurchases.any { purchase ->
                            processPurchase(
                                purchase = purchase,
                                restored = false,
                            )
                        }
                        if (!granted) {
                            preferencesRepository.setCachedRealProEnabled(false)
                        }
                    }

                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        _events.tryEmit(BillingEvent.PRO_CANCELLED)
                    }

                    else -> {
                        _events.tryEmit(BillingEvent.PRO_PURCHASE_FAILED)
                    }
                }
            }
        }

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    override val state: StateFlow<BillingState> = _state.asStateFlow()
    override val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    init {
        scope.launch {
            preferencesRepository.monetizationPreferences.collectLatest { preferences ->
                _state.update { current ->
                    current.copy(
                        isRealProEnabled = preferences.cachedRealProEnabled,
                        isMockProEnabled = current.canUseMockBilling && preferences.mockProEnabled,
                    )
                }
            }
        }
    }

    override suspend fun start() {
        if (!ensureConnected()) {
            _state.update { it.copy(isBillingSupported = false, isBillingReady = false, isProductAvailable = false) }
            return
        }
        refreshProductDetails()
        syncExistingPurchases(userInitiated = false)
    }

    override suspend fun refresh() {
        start()
    }

    override suspend fun restorePurchases() {
        if (!ensureConnected()) {
            _events.tryEmit(BillingEvent.BILLING_UNAVAILABLE)
            return
        }
        syncExistingPurchases(userInitiated = true)
    }

    override suspend fun launchProPurchase(activity: Activity): BillingLaunchResult {
        if (!ensureConnected()) {
            return BillingLaunchResult.Error(BillingEvent.BILLING_UNAVAILABLE)
        }

        if (state.value.isRealProEnabled) {
            return BillingLaunchResult.Error(BillingEvent.PRO_RESTORED)
        }

        if (productDetails == null) {
            refreshProductDetails()
        }

        val details = productDetails ?: return BillingLaunchResult.Error(BillingEvent.PRODUCT_UNAVAILABLE)
        val offer = details.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?: return BillingLaunchResult.Error(BillingEvent.PRODUCT_UNAVAILABLE)
        val offerToken = offer.offerToken
            ?: return BillingLaunchResult.Error(BillingEvent.PRODUCT_UNAVAILABLE)

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build(),
                ),
            )
            .build()

        val billingResult = withContext(Dispatchers.Main.immediate) {
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }

        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _state.update { it.copy(isPurchaseInProgress = true) }
            BillingLaunchResult.Launched
        } else {
            BillingLaunchResult.Error(
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.USER_CANCELED -> BillingEvent.PRO_CANCELLED
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> BillingEvent.PRO_RESTORED
                    else -> BillingEvent.PRO_PURCHASE_FAILED
                },
            )
        }
    }

    override suspend fun setMockProEnabled(enabled: Boolean) {
        if (!state.value.canUseMockBilling) return
        preferencesRepository.setMockProEnabled(enabled)
        _events.tryEmit(
            if (enabled) {
                BillingEvent.MOCK_PRO_ENABLED
            } else {
                BillingEvent.MOCK_PRO_DISABLED
            },
        )
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) {
            _state.update { it.copy(isBillingSupported = true, isBillingReady = true) }
            return true
        }

        return connectionMutex.withLock {
            if (billingClient.isReady) {
                _state.update { it.copy(isBillingSupported = true, isBillingReady = true) }
                return@withLock true
            }

            suspendCancellableCoroutine { continuation ->
                billingClient.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            val success = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                            _state.update {
                                it.copy(
                                    isBillingSupported = success,
                                    isBillingReady = success,
                                )
                            }
                            if (continuation.isActive) {
                                continuation.resume(success)
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            _state.update { it.copy(isBillingReady = false) }
                        }
                    },
                )
            }
        }
    }

    private suspend fun refreshProductDetails() {
        val result = queryProductDetails()
        val details = if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList.firstOrNull { detail ->
                detail.productId == BuildConfig.PRO_PRODUCT_ID
            }
        } else {
            null
        }

        productDetails = details
        _state.update { current ->
            current.copy(
                isProductAvailable = details != null,
                proPriceLabel = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice,
            )
        }
    }

    private suspend fun syncExistingPurchases(userInitiated: Boolean) {
        val purchasesResult = queryPurchases()
        if (purchasesResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            if (userInitiated) {
                _events.tryEmit(BillingEvent.BILLING_UNAVAILABLE)
            }
            return
        }

        val relevantPurchases = purchasesResult.purchasesList.filter(::isProPurchase)
        if (relevantPurchases.isEmpty()) {
            preferencesRepository.setCachedRealProEnabled(false)
            if (userInitiated) {
                _events.tryEmit(BillingEvent.RESTORE_NOT_FOUND)
            }
            return
        }

        var granted = false
        relevantPurchases.forEach { purchase ->
            granted = processPurchase(
                purchase = purchase,
                restored = userInitiated,
            ) || granted
        }

        if (!granted) {
            preferencesRepository.setCachedRealProEnabled(false)
            if (userInitiated) {
                _events.tryEmit(BillingEvent.RESTORE_NOT_FOUND)
            }
        }
    }

    private suspend fun processPurchase(
        purchase: Purchase,
        restored: Boolean,
    ): Boolean {
        return when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }

                val wasEnabled = state.value.isRealProEnabled
                preferencesRepository.setCachedRealProEnabled(true)

                if (!wasEnabled) {
                    _events.tryEmit(
                        if (restored) {
                            BillingEvent.PRO_RESTORED
                        } else {
                            BillingEvent.PRO_PURCHASED
                        },
                    )
                }
                true
            }

            Purchase.PurchaseState.PENDING -> {
                _events.tryEmit(BillingEvent.PRO_PENDING)
                false
            }

            else -> false
        }
    }

    private fun isProPurchase(purchase: Purchase): Boolean =
        purchase.products.contains(BuildConfig.PRO_PRODUCT_ID)

    private suspend fun queryProductDetails(): ProductDetailsQueryResult =
        suspendCancellableCoroutine { continuation ->
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BuildConfig.PRO_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    ),
                )
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
                if (continuation.isActive) {
                    continuation.resume(
                        ProductDetailsQueryResult(
                            billingResult = billingResult,
                            productDetailsList = queryProductDetailsResult.productDetailsList,
                        ),
                    )
                }
            }
        }

    private suspend fun queryPurchases(): PurchaseQueryResult =
        suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (continuation.isActive) {
                    continuation.resume(
                        PurchaseQueryResult(
                            billingResult = billingResult,
                            purchasesList = purchasesList,
                        ),
                    )
                }
            }
        }

    private suspend fun acknowledgePurchase(purchaseToken: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private data class PurchaseQueryResult(
        val billingResult: BillingResult,
        val purchasesList: List<Purchase>,
    )

    private data class ProductDetailsQueryResult(
        val billingResult: BillingResult,
        val productDetailsList: List<ProductDetails>,
    )
}
