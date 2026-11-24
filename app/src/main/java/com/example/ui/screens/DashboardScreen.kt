package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DashboardSummary
import com.example.data.model.LowStockAlert
import com.example.data.model.PackSize
import com.example.data.model.SpiceWithPacks
import com.example.ui.components.BilingualSectionHeader
import com.example.ui.components.CategoryBadge
import com.example.ui.components.KpiMetricCard
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    spices: List<SpiceWithPacks>,
    onGoToBilling: () -> Unit,
    onGoToUdhaarLedger: () -> Unit,
    onQuickRestock: (spiceId: Long, packSize: PackSize) -> Unit,
    onOpenSpiceInStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Hero Banner
        item {
            BrandHeroBanner(
                totalPackets = summary.totalPacketsInStock,
                totalWeightKg = summary.totalWeightKg,
                onNewBillClick = onGoToBilling,
                onAddStockClick = onOpenSpiceInStock
            )
        }

        // 2x2 Summary KPI Grid
        item {
            BilingualSectionHeader(
                titleEn = "Business Overview",
                titleHi = "आज का हिसाब-किताब"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        titleEn = "Today's Sales",
                        titleHi = "आज की बिक्री",
                        value = FormatUtils.formatRupees(summary.todayTotalSales),
                        subtitle = "${summary.todayOrdersCount} bills today",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        titleEn = "Today's Profit",
                        titleHi = "आज का मुनाफा",
                        value = FormatUtils.formatRupees(summary.todayEstimatedProfit),
                        subtitle = "Estimated net margin",
                        icon = Icons.Default.TrendingUp,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        titleEn = "Pending Udhaar",
                        titleHi = "कुल उधारी बाकी",
                        value = FormatUtils.formatRupees(summary.totalPendingUdhaar),
                        subtitle = if (summary.pendingUdhaarCount > 0) {
                            "${summary.pendingUdhaarCount} pending • Tap to collect"
                        } else {
                            "No pending dues"
                        },
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onGoToUdhaarLedger,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pending_udhaar_kpi_card")
                    )
                    KpiMetricCard(
                        titleEn = "Godown Stock Value",
                        titleHi = "गोदाम माल कीमत",
                        value = FormatUtils.formatRupees(summary.totalInventoryCostValue),
                        subtitle = "Sell val: ${FormatUtils.formatRupees(summary.totalInventorySellingValue)}",
                        icon = Icons.Default.Inventory2,
                        accentColor = MaterialTheme.colorScheme.onSurface,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low Stock Alert Box
        item {
            LowStockAlertBox(
                alerts = summary.lowStockAlerts,
                onQuickRestock = onQuickRestock
            )
        }

        // Quick Stock Overview Header
        item {
            BilingualSectionHeader(
                titleEn = "Quick Stock Overview (${spices.size} Spices)",
                titleHi = "सभी मसालों का स्टॉक व कुल वजन",
                trailing = {
                    FilledTonalButton(onClick = onOpenSpiceInStock) {
                        Text("Manage")
                    }
                }
            )
        }

        // Quick Stock Overview Rows
        items(spices, key = { it.id }) { spice ->
            QuickSpiceOverviewRow(spice = spice)
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun BrandHeroBanner(
    totalPackets: Int,
    totalWeightKg: Double,
    onNewBillClick: () -> Unit,
    onAddStockClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_spice_banner),
                contentDescription = "Chanouti Masala authentic Indian spices",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xEB221308),
                                Color(0xCC2C180A),
                                Color(0x441B0E05)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0x33FBBF24)
                    ) {
                        Text(
                            text = "OFFLINE FACTORY & SHOP LEDGER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Chanouti Masala",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "चुनौती मसाला • $totalPackets pkts (${FormatUtils.formatKg(totalWeightKg)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFDE68A)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNewBillClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("hero_new_sale_button")
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("New Bill", style = MaterialTheme.typography.labelLarge)
                    }

                    FilledTonalButton(
                        onClick = onAddStockClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xE6FFF7ED),
                            contentColor = Color(0xFF7C2D12)
                        ),
                        modifier = Modifier.testTag("hero_add_stock_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stock", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockAlertBox(
    alerts: List<LowStockAlert>,
    onQuickRestock: (spiceId: Long, packSize: PackSize) -> Unit
) {
    val hasAlerts = alerts.isNotEmpty()
    val containerBg = if (hasAlerts) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
    }
    val borderColor = if (hasAlerts) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Low Stock Alert",
                    tint = if (hasAlerts) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasAlerts) {
                            "Low Stock Alert (${alerts.size} packs < 10)"
                        } else {
                            "All Stock Healthy (स्टॉक पूरा है)"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasAlerts) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (hasAlerts) {
                            "कम स्टॉक अलर्ट • पैकिंग बढ़ाएं"
                        } else {
                            "Every spice pack has 10+ packets ready"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (hasAlerts) {
                Spacer(modifier = Modifier.height(10.dp))
                alerts.forEachIndexed { index, alert ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${alert.spiceNameEn} (${alert.packSize.label})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${alert.spiceNameHi} • Only ${alert.stockPackets} pkts left",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (alert.stockPackets <= 3) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                        FilledTonalButton(
                            onClick = { onQuickRestock(alert.spiceId, alert.packSize) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pack")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSpiceOverviewRow(spice: SpiceWithPacks) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = spice.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CategoryBadge(category = spice.category)
                }
                Text(
                    text = spice.nameHi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${spice.totalPackets} pkts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = FormatUtils.formatKg(spice.totalWeightKg),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
