package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PackSize
import com.example.data.model.PaymentStatus
import com.example.data.model.SpiceWithPacks
import com.example.ui.components.BilingualSectionHeader
import com.example.ui.viewmodel.BillingUiState
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BillingScreen(
    spices: List<SpiceWithPacks>,
    recentCustomers: List<String>,
    billingState: BillingUiState,
    onCustomerNameChange: (String) -> Unit,
    onAddItemToCart: (SpiceWithPacks, PackSize, Int, Double) -> Boolean,
    onRemoveCartItem: (Long, PackSize) -> Unit,
    onClearCart: () -> Unit,
    onPaymentStatusChange: (PaymentStatus) -> Unit,
    onPartialPaidChange: (String) -> Unit,
    onConfirmSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (spices.isEmpty()) return

    var selectedSpiceId by remember { mutableStateOf(spices.first().id) }
    var selectedPackSize by remember { mutableStateOf(PackSize.G100) }
    var spiceDropdownExpanded by remember { mutableStateOf(false) }

    val selectedSpice = spices.firstOrNull { it.id == selectedSpiceId } ?: spices.first()
    val selectedPack = selectedSpice.packFor(selectedPackSize)

    val alreadyInCartQty = billingState.cartItems
        .firstOrNull { it.spiceId == selectedSpice.id && it.packSize == selectedPackSize }
        ?.quantityPackets ?: 0
    val availableLeft = ((selectedPack?.stockPackets ?: 0) - alreadyInCartQty).coerceAtLeast(0)

    var quantityText by remember { mutableStateOf("1") }
    var priceOverrideText by remember(selectedSpice.id, selectedPackSize) {
        mutableStateOf(
            selectedPack?.sellingPrice?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: "0"
        )
    }

    LaunchedEffect(selectedPack?.id, selectedPack?.sellingPrice) {
        if (selectedPack != null) {
            val sp = selectedPack.sellingPrice
            priceOverrideText = if (sp % 1.0 == 0.0) sp.toInt().toString() else sp.toString()
        }
    }

    val qtyInt = quantityText.toIntOrNull() ?: 0
    val priceDouble = priceOverrideText.toDoubleOrNull() ?: 0.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Customer / Shopkeeper Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BilingualSectionHeader(
                        titleEn = "Shopkeeper / Customer",
                        titleHi = "दुकानदार / ग्राहक का नाम"
                    )
                    OutlinedTextField(
                        value = billingState.customerName,
                        onValueChange = onCustomerNameChange,
                        placeholder = { Text("Cash Customer (नकद ग्राहक)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            if (billingState.customerName.isNotEmpty()) {
                                IconButton(onClick = { onCustomerNameChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear customer")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("billing_customer_input")
                    )

                    if (recentCustomers.isNotEmpty()) {
                        Text(
                            text = "Recent Shopkeepers (पिछले दुकानदार):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = billingState.customerName.isBlank(),
                                onClick = { onCustomerNameChange("") },
                                label = { Text("Cash Customer") }
                            )
                            recentCustomers.forEach { customer ->
                                FilterChip(
                                    selected = billingState.customerName.equals(customer, ignoreCase = true),
                                    onClick = { onCustomerNameChange(customer) },
                                    label = { Text(customer) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Add Spice to Bill Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BilingualSectionHeader(
                        titleEn = "Select Spice & Pack",
                        titleHi = "मसाला और पैकेट चुनें"
                    )

                    ExposedDropdownMenuBox(
                        expanded = spiceDropdownExpanded,
                        onExpandedChange = { spiceDropdownExpanded = !spiceDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedSpice.nameEn} (${selectedSpice.nameHi})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Spice (मसाला)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = spiceDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("billing_spice_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = spiceDropdownExpanded,
                            onDismissRequest = { spiceDropdownExpanded = false }
                        ) {
                            spices.forEach { spice ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${spice.nameEn} (${spice.nameHi})")
                                            Text(
                                                "${spice.totalPackets} pkts",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSpiceId = spice.id
                                        spiceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Pack Size & Available Stock (पैकेट साइज़)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PackSize.entries.forEach { size ->
                            val pack = selectedSpice.packFor(size)
                            val inCart = billingState.cartItems
                                .firstOrNull { it.spiceId == selectedSpice.id && it.packSize == size }
                                ?.quantityPackets ?: 0
                            val left = ((pack?.stockPackets ?: 0) - inCart).coerceAtLeast(0)
                            FilterChip(
                                selected = selectedPackSize == size,
                                onClick = { selectedPackSize = size },
                                label = {
                                    Text(
                                        "${size.label} • $left",
                                        fontWeight = if (selectedPackSize == size) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Quantity Stepper & Selling Price Override
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "Quantity (पैकेट) • Left: $availableLeft",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (availableLeft == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        val next = (qtyInt - 1).coerceAtLeast(1)
                                        quantityText = next.toString()
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
                                }
                                OutlinedTextField(
                                    value = quantityText,
                                    onValueChange = { quantityText = it.filter(Char::isDigit) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("billing_quantity_input")
                                )
                                FilledTonalIconButton(
                                    onClick = {
                                        val next = (qtyInt + 1).coerceAtMost(availableLeft.coerceAtLeast(1))
                                        quantityText = next.toString()
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(0.9f)) {
                            val costHint = selectedPack?.costPrice ?: 0.0
                            Text(
                                text = "Rate ₹ (Cost ${FormatUtils.formatRupees(costHint)})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = priceOverrideText,
                                onValueChange = { priceOverrideText = it.filter { c -> c.isDigit() || c == '.' } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("billing_price_input")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedPack != null) {
                                val added = onAddItemToCart(selectedSpice, selectedPackSize, qtyInt, priceDouble)
                                if (added) {
                                    quantityText = "1"
                                }
                            }
                        },
                        enabled = availableLeft > 0 && qtyInt in 1..availableLeft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_to_bill_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        val previewTotal = qtyInt * priceDouble
                        Text(
                            text = if (availableLeft == 0) {
                                "Out of Stock (स्टॉक खत्म)"
                            } else {
                                "Add to Bill (${FormatUtils.formatRupees(previewTotal)}) • जोड़ें"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 3. Current Cart / Bill Summary & Checkout
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (billingState.cartItems.isNotEmpty()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BilingualSectionHeader(
                        titleEn = "Current Bill (${billingState.cartItems.size} items)",
                        titleHi = "मौजूदा बिल",
                        trailing = {
                            if (billingState.cartItems.isNotEmpty()) {
                                OutlinedButton(onClick = onClearCart) {
                                    Text("Clear")
                                }
                            }
                        }
                    )

                    if (billingState.cartItems.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "No items in bill yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "ऊपर से मसाला चुनकर बिल में जोड़ें",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        billingState.cartItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${item.spiceNameEn} (${item.packSize.label})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.quantityPackets} pkts × ${FormatUtils.formatRupees(item.unitSellingPrice)} • Profit ${FormatUtils.formatRupees(item.lineProfit)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = FormatUtils.formatRupees(item.lineTotal),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = { onRemoveCartItem(item.spiceId, item.packSize) }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove ${item.spiceNameEn}",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Bill (कुल राशि)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                FormatUtils.formatRupees(billingState.subtotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Profit (अनुमानित मुनाफा)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                            Text(
                                FormatUtils.formatRupees(billingState.estimatedProfit),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Text(
                            text = "Payment Status (भुगतान की स्थिति)",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentStatus.entries.forEach { status ->
                                val selected = billingState.paymentStatus == status
                                FilterChip(
                                    selected = selected,
                                    onClick = { onPaymentStatusChange(status) },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(status.labelEn, fontWeight = FontWeight.SemiBold)
                                            Text(status.labelHi, style = MaterialTheme.typography.labelSmall)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("payment_status_${status.name.lowercase()}")
                                )
                            }
                        }

                        if (billingState.paymentStatus == PaymentStatus.PARTIAL) {
                            OutlinedTextField(
                                value = billingState.partialPaidInput,
                                onValueChange = onPartialPaidChange,
                                label = { Text("Amount Paid Today (₹ आज मिला)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Balance Udhaar (बाकी उधारी): ${FormatUtils.formatRupees(billingState.balanceUdhaar)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (billingState.paymentStatus == PaymentStatus.UDHAAR) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "Full ${FormatUtils.formatRupees(billingState.subtotal)} will be added to Udhaar Ledger.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onConfirmSale,
                            enabled = !billingState.isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("confirm_sale_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Confirm Sale (बिक्री पक्की करें)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
