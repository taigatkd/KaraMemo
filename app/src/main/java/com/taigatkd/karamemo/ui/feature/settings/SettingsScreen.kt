package com.taigatkd.karamemo.ui.feature.settings

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taigatkd.karamemo.R
import com.taigatkd.karamemo.ui.components.KaraMemoRecordCard
import com.taigatkd.karamemo.ui.theme.KaraMemoTheme

data class SettingsBillingUi(
    val isProEnabled: Boolean,
    val hasRealProPurchase: Boolean,
    val isMockProEnabled: Boolean,
    val canUseMockBilling: Boolean,
    val isBillingReady: Boolean,
    val isProductAvailable: Boolean,
    val isPurchaseInProgress: Boolean,
    val proPriceLabel: String?,
    val currentSongCount: Int,
    val freeSongLimit: Int,
) {
    val remainingFreeSongs: Int
        get() = (freeSongLimit - currentSongCount).coerceAtLeast(0)
}

enum class SettingsPage {
    ROOT,
    PRO,
    PRIVACY,
    TERMS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    page: SettingsPage,
    billingUi: SettingsBillingUi,
    onDismiss: () -> Unit,
    onOpenKaraokeSettings: () -> Unit,
    onOpenPage: (SettingsPage) -> Unit,
    onBuyPro: () -> Unit,
    onRestorePurchases: () -> Unit,
    onSetMockProEnabled: (Boolean) -> Unit,
) {
    val title = when (page) {
        SettingsPage.ROOT -> stringResource(R.string.title_settings)
        SettingsPage.PRO -> stringResource(R.string.title_pro_plan)
        SettingsPage.PRIVACY -> stringResource(R.string.title_privacy_policy)
        SettingsPage.TERMS -> stringResource(R.string.title_terms_of_use)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (page == SettingsPage.ROOT) {
                                    onDismiss()
                                } else {
                                    onOpenPage(SettingsPage.ROOT)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            when (page) {
                SettingsPage.ROOT -> SettingsRootContent(
                    modifier = Modifier.padding(innerPadding),
                    billingUi = billingUi,
                    onOpenKaraokeSettings = onOpenKaraokeSettings,
                    onOpenPage = onOpenPage,
                )

                SettingsPage.PRO -> SettingsProContent(
                    modifier = Modifier.padding(innerPadding),
                    billingUi = billingUi,
                    onBuyPro = onBuyPro,
                    onRestorePurchases = onRestorePurchases,
                    onSetMockProEnabled = onSetMockProEnabled,
                )

                SettingsPage.PRIVACY -> SettingsDocumentContent(
                    modifier = Modifier.padding(innerPadding),
                    rawResId = R.raw.privacy_policy,
                )

                SettingsPage.TERMS -> SettingsDocumentContent(
                    modifier = Modifier.padding(innerPadding),
                    rawResId = R.raw.terms_of_use,
                )
            }
        }
    }
}

@Composable
private fun SettingsRootContent(
    billingUi: SettingsBillingUi,
    onOpenKaraokeSettings: () -> Unit,
    onOpenPage: (SettingsPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        SettingsRowItem(
            icon = { Icon(Icons.Outlined.GraphicEq, contentDescription = null) },
            title = stringResource(R.string.settings_item_karaoke_title),
            summary = stringResource(R.string.settings_item_karaoke_summary),
            onClick = onOpenKaraokeSettings,
        ),
        SettingsRowItem(
            icon = { Icon(Icons.Outlined.Shield, contentDescription = null) },
            title = stringResource(R.string.settings_item_privacy_title),
            summary = stringResource(R.string.settings_item_privacy_summary),
            onClick = { onOpenPage(SettingsPage.PRIVACY) },
        ),
        SettingsRowItem(
            icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
            title = stringResource(R.string.settings_item_terms_title),
            summary = stringResource(R.string.settings_item_terms_summary),
            onClick = { onOpenPage(SettingsPage.TERMS) },
        ),
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsPlanCard(
                billingUi = billingUi,
                onOpenPage = { onOpenPage(SettingsPage.PRO) },
            )
        }

        items(rows) { row ->
            SettingsRow(item = row)
        }
    }
}

@Composable
private fun SettingsPlanCard(
    billingUi: SettingsBillingUi,
    onOpenPage: (() -> Unit)?,
) {
    KaraMemoRecordCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(
                        text = stringResource(R.string.settings_item_pro_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_item_pro_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            SettingsPlanRow(
                label = stringResource(R.string.label_current_plan),
                value = if (billingUi.isProEnabled) {
                    stringResource(R.string.label_plan_pro)
                } else {
                    stringResource(R.string.label_plan_free)
                },
                emphasized = true,
            )
            SettingsPlanRow(
                label = stringResource(R.string.label_plan_song_limit),
                value = if (billingUi.isProEnabled) {
                    stringResource(R.string.label_plan_unlimited)
                } else {
                    stringResource(
                        R.string.label_plan_song_limit_value,
                        billingUi.freeSongLimit,
                    )
                },
            )
            SettingsPlanRow(
                label = stringResource(R.string.label_plan_remaining),
                value = if (billingUi.isProEnabled) {
                    stringResource(R.string.label_plan_unlimited)
                } else {
                    stringResource(
                        R.string.label_plan_remaining_value,
                        billingUi.remainingFreeSongs,
                    )
                },
            )
            SettingsPlanRow(
                label = stringResource(R.string.label_plan_pro),
                value = billingUi.proPriceLabel ?: stringResource(R.string.label_plan_pro_price),
            )

            Text(
                text = stringResource(R.string.label_plan_upgrade_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (onOpenPage != null) {
                TextButton(
                    onClick = onOpenPage,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.action_view_pro_details))
                }
            }
        }
    }
}

@Composable
private fun SettingsPlanRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (emphasized) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SettingsProContent(
    billingUi: SettingsBillingUi,
    onBuyPro: () -> Unit,
    onRestorePurchases: () -> Unit,
    onSetMockProEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val documentText = rememberSettingsDocument(R.raw.pro_plan)
    val canBuyPro = !billingUi.isProEnabled && billingUi.isBillingReady && billingUi.isProductAvailable

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SettingsPlanCard(
                billingUi = billingUi,
                onOpenPage = null,
            )
        }

        item {
            KaraMemoRecordCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onBuyPro,
                        enabled = canBuyPro && !billingUi.isPurchaseInProgress,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (billingUi.isProEnabled) {
                                stringResource(R.string.label_plan_pro_active)
                            } else {
                                stringResource(R.string.action_buy_pro)
                            },
                        )
                    }

                    OutlinedButton(
                        onClick = onRestorePurchases,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.action_restore_purchases))
                    }

                    if (billingUi.canUseMockBilling) {
                        OutlinedButton(
                            onClick = { onSetMockProEnabled(!billingUi.isMockProEnabled) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = if (billingUi.isMockProEnabled) {
                                    stringResource(R.string.action_disable_mock_pro)
                                } else {
                                    stringResource(R.string.action_enable_mock_pro)
                                },
                            )
                        }
                    }

                    val statusMessage = when {
                        billingUi.isProEnabled && billingUi.hasRealProPurchase ->
                            stringResource(R.string.label_plan_status_real_purchase)
                        billingUi.isProEnabled && billingUi.isMockProEnabled ->
                            stringResource(R.string.label_plan_status_mock_purchase)
                        !billingUi.isBillingReady ->
                            stringResource(R.string.label_plan_status_billing_connecting)
                        !billingUi.isProductAvailable ->
                            stringResource(R.string.label_plan_status_product_unavailable)
                        else ->
                            stringResource(R.string.label_plan_status_ready)
                    }

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            KaraMemoRecordCard {
                Text(
                    text = documentText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsRowItem,
) {
    KaraMemoRecordCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = item.onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item.icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsDocumentContent(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
) {
    val text = rememberSettingsDocument(rawResId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun rememberSettingsDocument(
    @RawRes rawResId: Int,
): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(rawResId, configuration) {
        context.readRawText(rawResId)
    }
}

private fun Context.readRawText(@RawRes rawResId: Int): String =
    resources.openRawResource(rawResId).bufferedReader().use { it.readText() }

private data class SettingsRowItem(
    val icon: @Composable () -> Unit,
    val title: String,
    val summary: String,
    val onClick: () -> Unit,
)

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SettingsRootPreview() {
    KaraMemoTheme {
        SettingsScreen(
            page = SettingsPage.ROOT,
            billingUi = SettingsBillingUi(
                isProEnabled = false,
                hasRealProPurchase = false,
                isMockProEnabled = false,
                canUseMockBilling = true,
                isBillingReady = true,
                isProductAvailable = true,
                isPurchaseInProgress = false,
                proPriceLabel = "JPY 480",
                currentSongCount = 64,
                freeSongLimit = 100,
            ),
            onDismiss = {},
            onOpenKaraokeSettings = {},
            onOpenPage = {},
            onBuyPro = {},
            onRestorePurchases = {},
            onSetMockProEnabled = {},
        )
    }
}
