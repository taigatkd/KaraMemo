package com.taigatkd.karamemo.billing

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

const val FREE_SONG_LIMIT = 75

data class BillingState(
    val isBillingSupported: Boolean = false,
    val isBillingReady: Boolean = false,
    val isProductAvailable: Boolean = false,
    val proPriceLabel: String? = null,
    val isRealProEnabled: Boolean = false,
    val isMockProEnabled: Boolean = false,
    val isPurchaseInProgress: Boolean = false,
    val canUseMockBilling: Boolean = false,
) {
    val isProEnabled: Boolean
        get() = isRealProEnabled || (canUseMockBilling && isMockProEnabled)
}

enum class BillingEvent {
    PRO_PURCHASED,
    PRO_RESTORED,
    PRO_PENDING,
    PRO_CANCELLED,
    PRO_PURCHASE_FAILED,
    RESTORE_NOT_FOUND,
    BILLING_UNAVAILABLE,
    PRODUCT_UNAVAILABLE,
    MOCK_PRO_ENABLED,
    MOCK_PRO_DISABLED,
}

sealed interface BillingLaunchResult {
    data object Launched : BillingLaunchResult
    data class Error(val event: BillingEvent) : BillingLaunchResult
}

interface BillingRepository {
    val state: StateFlow<BillingState>
    val events: SharedFlow<BillingEvent>

    suspend fun start()
    suspend fun refresh()
    suspend fun restorePurchases()
    suspend fun launchProPurchase(activity: Activity): BillingLaunchResult
    suspend fun setMockProEnabled(enabled: Boolean)
}
