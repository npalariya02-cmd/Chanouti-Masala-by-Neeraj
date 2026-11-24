package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PackSize
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpiceCategory
import com.example.data.model.SpiceWithPacks
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddProductionDialog(
    spices: List<SpiceWithPacks>,
    initialSpiceId: Long?,
    initialPackSize: PackSize,
    onDismiss: () -> Unit,
    onSave: (spiceId: Long, packSize: PackSize, addedPackets: Int, costPrice: Double, sellingPrice: Double) -> Unit
) {
    if (spices.isEmpty()) return
    var selectedSpiceId by remember {
        mutableStateOf(initialSpiceId ?: spices.first().id)
    }
    var selectedPackSize by remember { mutableStateOf(initialPackSize) }
    var spiceMenuExpanded by remember { mutableStateOf(false) }

    val selectedSpice = spices.firstOrNull { it.id == selectedSpiceId } ?: spices.first()
    val currentPack = selectedSpice.packFor(selectedPackSize)

    var addedPacketsText by remember { mutableStateOf("25") }
    var costText by remember(selectedSpiceId, selectedPackSize) {
        mutableStateOf((currentPack?.costPrice ?: 0.0).toCleanNumberString())
    }
    var sellText by remember(selectedSpiceId, selectedPackSize) {
        mutableStateOf((currentPack?.sellingPrice ?: 0.0).toCleanNumberString())
    }

    LaunchedEffect(currentPack?.id) {
        if (currentPack != null) {
            costText = currentPack.costPrice.toCleanNumberString()
            sellText = currentPack.sellingPrice.toCleanNumberString()
        }
    }

    val addedInt = addedPacketsText.toIntOrNull() ?: 0
    val currentStock = currentPack?.stockPackets ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add Production / Stock", fontWeight = FontWeight.Bold)
                Text(
                    "नया पैक्ड माल जोड़ें / कीमत बदलें",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = spiceMenuExpanded,
                    onExpandedChange = { spiceMenuExpanded = !spiceMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedSpice.nameEn} (${selectedSpice.nameHi})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Spice (मसाला)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = spiceMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = spiceMenuExpanded,
                        onDismissRequest = { spiceMenuExpanded = false }
                    ) {
                        spices.forEach { spice ->
                            DropdownMenuItem(
                                text = { Text("${spice.nameEn} (${spice.nameHi})") },
                                onClick = {
                                    selectedSpiceId = spice.id
                                    spiceMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Pack Size (पैकेट साइज़)",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PackSize.entries.forEach { size ->
                        val pkStock = selectedSpice.packFor(size)?.stockPackets ?: 0
                        FilterChip(
                            selected = selectedPackSize == size,
                            onClick = { selectedPackSize = size },
                            label = { Text("${size.label} ($pkStock)") }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Current Stock: $currentStock pkts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "New: ${currentStock + addedInt} pkts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = addedPacketsText,
                    onValueChange = { addedPacketsText = it.filter(Char::isDigit) },
                    label = { Text("Add Packets (+ पैकेट जोड़ें)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("production_packets_input")
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 10, 25, 50, 100).forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                val next = if (preset == 0) 0 else addedInt + preset
                                addedPacketsText = next.toString()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (preset == 0) "0 (Price Only)" else "+$preset")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cost ₹ (लागत)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellText,
                        onValueChange = { sellText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Sell ₹ (बिक्री)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costText.toDoubleOrNull() ?: (currentPack?.costPrice ?: 0.0)
                    val sell = sellText.toDoubleOrNull() ?: (currentPack?.sellingPrice ?: 0.0)
                    onSave(selectedSpiceId, selectedPackSize, addedInt, cost, sell)
                },
                modifier = Modifier.testTag("save_production_button")
            ) {
                Text("Save Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSpiceDialog(
    spice: SpiceWithPacks,
    onDismiss: () -> Unit,
    onSave: (nameEn: String, nameHi: String, category: SpiceCategory) -> Unit
) {
    var nameEn by remember { mutableStateOf(spice.nameEn) }
    var nameHi by remember { mutableStateOf(spice.nameHi) }
    var category by remember { mutableStateOf(spice.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Spice (मसाला बदलें)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("English Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameHi,
                    onValueChange = { nameHi = it },
                    label = { Text("Hindi Name (हिंदी नाम)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category (श्रेणी)", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpiceCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${cat.labelEn} (${cat.labelHi})") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameEn, nameHi, category) },
                enabled = nameEn.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNewSpiceDialog(
    onDismiss: () -> Unit,
    onAdd: (
        nameEn: String,
        nameHi: String,
        category: SpiceCategory,
        initialPacks: Map<PackSize, Triple<Int, Double, Double>>
    ) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameHi by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SpiceCategory.BLENDED) }

    val defaultPacks = remember {
        mutableStateMapOf(
            PackSize.G10 to Triple("20", "4", "6"),
            PackSize.G50 to Triple("20", "18", "26"),
            PackSize.G100 to Triple("20", "34", "48"),
            PackSize.G200 to Triple("10", "65", "92")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add New Spice", fontWeight = FontWeight.Bold)
                Text(
                    "नया मसाला जोड़ें",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Spice Name (English)*") },
                    placeholder = { Text("e.g. Chat Masala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameHi,
                    onValueChange = { nameHi = it },
                    label = { Text("Hindi Name (हिंदी नाम)") },
                    placeholder = { Text("जैसे चाट मसाला") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category (श्रेणी)", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpiceCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.labelEn) }
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    "Initial Stock & Pricing (शुरुआती स्टॉक व रेट)",
                    style = MaterialTheme.typography.labelLarge
                )
                PackSize.entries.forEach { size ->
                    val current = defaultPacks[size] ?: Triple("0", "0", "0")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = size.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp)
                        )
                        OutlinedTextField(
                            value = current.first,
                            onValueChange = { v ->
                                defaultPacks[size] = current.copy(first = v.filter(Char::isDigit))
                            },
                            label = { Text("Pkts") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = current.second,
                            onValueChange = { v ->
                                defaultPacks[size] = current.copy(second = v.filter { c -> c.isDigit() || c == '.' })
                            },
                            label = { Text("Cost") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = current.third,
                            onValueChange = { v ->
                                defaultPacks[size] = current.copy(third = v.filter { c -> c.isDigit() || c == '.' })
                            },
                            label = { Text("Sell") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = PackSize.entries.associateWith { size ->
                        val t = defaultPacks[size] ?: Triple("0", "0", "0")
                        Triple(
                            t.first.toIntOrNull() ?: 0,
                            t.second.toDoubleOrNull() ?: 0.0,
                            t.third.toDoubleOrNull() ?: 0.0
                        )
                    }
                    onAdd(nameEn, nameHi.ifBlank { nameEn }, category, parsed)
                },
                enabled = nameEn.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Spice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SaleReceiptDialog(
    sale: SaleWithDetails,
    onDismiss: () -> Unit,
    onShareWhatsApp: (SaleWithDetails) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Bill Saved",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Bill Confirmed!", fontWeight = FontWeight.Bold)
                Text(
                    "${sale.invoiceNumber} • ${sale.customerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Bill (कुल बिल)", fontWeight = FontWeight.Bold)
                    Text(
                        FormatUtils.formatRupees(sale.totalAmount),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Paid (जमा)", style = MaterialTheme.typography.bodyMedium)
                    Text(FormatUtils.formatRupees(sale.paidAmount), style = MaterialTheme.typography.bodyMedium)
                }
                if (sale.hasPendingUdhaar) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Pending Udhaar (बाकी)",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            FormatUtils.formatRupees(sale.pendingUdhaar),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Est. Profit (मुनाफा)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        FormatUtils.formatRupees(sale.estimatedProfit),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onShareWhatsApp(sale) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.testTag("whatsapp_share_receipt_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share on WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ReceivePaymentDialog(
    sale: SaleWithDetails,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, note: String) -> Unit
) {
    val due = sale.pendingUdhaar
    var amountText by remember { mutableStateOf(due.toCleanNumberString()) }
    var noteText by remember { mutableStateOf("Cash / UPI") }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Receive Payment", fontWeight = FontWeight.Bold)
                Text(
                    "उधारी पैसा जमा करें • ${sale.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pending Udhaar (बाकी):", fontWeight = FontWeight.Medium)
                        Text(
                            FormatUtils.formatRupees(due),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount Received (₹ मिला)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { amountText = due.toCleanNumberString() }
                    ) {
                        Text("Full (${FormatUtils.formatRupees(due)})")
                    }
                    OutlinedButton(
                        onClick = { amountText = (due / 2.0).toCleanNumberString() }
                    ) {
                        Text("Half (आधा)")
                    }
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Mode / Note (नकद / UPI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmPayment(parsedAmount.coerceAtMost(due), noteText) },
                enabled = parsedAmount > 0.0,
                modifier = Modifier.testTag("confirm_udhaar_payment_button")
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingsAndBackupDialog(
    onDismiss: () -> Unit,
    onSaveCsvToDevice: () -> Unit,
    onShareCsv: () -> Unit,
    onResetAllData: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Data? (डेटा रीसेट?)", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete all recorded sales and udhaar entries and restore the 13 default Chanouti Masala spices. Export a CSV backup first if needed.\n\nसभी बिक्री और उधारी का रिकॉर्ड हट जाएगा और डिफ़ॉल्ट 13 मसाले वापस आ जाएंगे।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetAllData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Yes, Reset Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Settings & Backup", fontWeight = FontWeight.Bold)
                Text(
                    "डेटा बैकअप और सेटिंग्स",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "100% Offline Storage",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "All inventory, sales, and udhaar data is saved automatically on your phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onSaveCsvToDevice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_csv_save_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Backup / Save CSV (सेव करें)")
                }

                OutlinedButton(
                    onClick = onShareCsv,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_csv_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share CSV Report (शेयर करें)")
                }

                HorizontalDivider()

                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_data_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Data (डेटा रीसेट करें)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun Double.toCleanNumberString(): String {
    return if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()
}
