package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LedgerFilter
import com.example.data.model.SaleWithDetails
import com.example.ui.components.PaymentStatusBadge
import com.example.util.FormatUtils

@Composable
fun LedgerScreen(
    sales: List<SaleWithDetails>,
    filter: LedgerFilter,
    searchQuery: String,
    onFilterChange: (LedgerFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onReceivePayment: (SaleWithDetails) -> Unit,
    onShareWhatsApp: (SaleWithDetails) -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startOfToday = remember { FormatUtils.startOfTodayMillis() }

    val filteredSales = remember(sales, filter, searchQuery, startOfToday) {
        sales.filter { sale ->
            val matchesFilter = when (filter) {
                LedgerFilter.ALL -> true
                LedgerFilter.TODAY -> sale.createdAtMillis >= startOfToday
                LedgerFilter.UDHAAR_PENDING -> sale.hasPendingUdhaar
            }
            val matchesSearch = searchQuery.isBlank() ||
                sale.customerName.contains(searchQuery, ignoreCase = true) ||
                sale.invoiceNumber.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val filteredTotal = filteredSales.sumOf { it.totalAmount }
    val filteredPaid = filteredSales.sumOf { it.paidAmount }
    val filteredDue = filteredSales.sumOf { it.pendingUdhaar }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search + Export CSV
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search party or bill #...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("ledger_export_csv_button"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("CSV")
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LedgerFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { onFilterChange(f) },
                        label = { Text("${f.labelEn} (${f.labelHi})") },
                        modifier = Modifier.testTag("ledger_filter_${f.name.lowercase()}")
                    )
                }
            }
        }

        // Summary Strip
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Billed (${filteredSales.size})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupees(filteredTotal), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Received (मिला)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupees(filteredPaid), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Udhaar (बाकी)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            FormatUtils.formatRupees(filteredDue),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (filteredDue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (filteredSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No sales records found",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "अभी कोई बिक्री दर्ज नहीं है। 'माल बेचें' से नया बिल बनाएं।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSales, key = { it.id }) { sale ->
                SaleLedgerCard(
                    sale = sale,
                    onReceivePayment = { onReceivePayment(sale) },
                    onShareWhatsApp = { onShareWhatsApp(sale) }
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun SaleLedgerCard(
    sale: SaleWithDetails,
    onReceivePayment: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (sale.hasPendingUdhaar) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${sale.invoiceNumber} • ${FormatUtils.formatDateShort(sale.createdAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(status = sale.paymentStatus)
            }

            HorizontalDivider()

            sale.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.spiceNameEn} (${item.packSize.label}) × ${item.quantityPackets}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = FormatUtils.formatRupees(item.lineTotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total: ${FormatUtils.formatRupees(sale.totalAmount)}", fontWeight = FontWeight.Bold)
                    Text(
                        "Paid: ${FormatUtils.formatRupees(sale.paidAmount)} • Profit: ${FormatUtils.formatRupees(sale.estimatedProfit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sale.hasPendingUdhaar) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Due: ${FormatUtils.formatRupees(sale.pendingUdhaar)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "उधारी बाकी",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareWhatsApp,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", maxLines = 1)
                }

                if (sale.hasPendingUdhaar) {
                    Button(
                        onClick = onReceivePayment,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("receive_payment_button_${sale.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Receive Payment", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
