package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.PackSize
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpiceWithPacks
import java.io.File
import java.io.OutputStream

object ExportAndShareHelper {

    fun buildWhatsAppReceipt(sale: SaleWithDetails): String {
        val sb = StringBuilder()
        sb.appendLine("*CHANOUTI MASALA (चुनौती मसाला)*")
        sb.appendLine("Pure & Fresh Indian Spices")
        sb.appendLine("--------------------------------")
        sb.appendLine("Bill No: ${sale.invoiceNumber}")
        sb.appendLine("Date: ${FormatUtils.formatDateTime(sale.createdAtMillis)}")
        sb.appendLine("Party: ${sale.customerName}")
        sb.appendLine("--------------------------------")
        sale.items.forEachIndexed { idx, item ->
            sb.appendLine(
                "${idx + 1}. ${item.spiceNameEn} (${item.packSize.label}) x ${item.quantityPackets} @ ${FormatUtils.formatRupees(item.unitSellingPrice)} = ${FormatUtils.formatRupees(item.lineTotal)}"
            )
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("*Total Bill:* ${FormatUtils.formatRupees(sale.totalAmount)}")
        sb.appendLine("Paid (${sale.paymentStatus.labelEn}): ${FormatUtils.formatRupees(sale.paidAmount)}")
        if (sale.hasPendingUdhaar) {
            sb.appendLine("*Balance Udhaar (बाकी):* ${FormatUtils.formatRupees(sale.pendingUdhaar)}")
        } else {
            sb.appendLine("Status: FULLY PAID (पूरा जमा)")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("Thank you for your business! धन्यवाद!")
        return sb.toString()
    }

    fun shareReceiptOnWhatsApp(context: Context, sale: SaleWithDetails) {
        val text = buildWhatsAppReceipt(sale)
        val whatsAppIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        val pm = context.packageManager
        if (whatsAppIntent.resolveActivity(pm) != null) {
            context.startActivity(whatsAppIntent)
        } else {
            val whatsAppBizIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp.w4b")
            }
            if (whatsAppBizIntent.resolveActivity(pm) != null) {
                context.startActivity(whatsAppBizIntent)
            } else {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    },
                    "Share Bill Receipt"
                )
                context.startActivity(chooser)
            }
        }
    }

    fun buildCombinedCsvReport(
        spices: List<SpiceWithPacks>,
        sales: List<SaleWithDetails>
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for clean Devanagari display in Microsoft Excel
        sb.append('\uFEFF')

        sb.appendLine("=== CHANOUTI MASALA STOCK REPORT (गोदाम स्टॉक) ===")
        sb.appendLine("Spice Name (EN),Spice Name (HI),Category,Pack Size,Stock (Packets),Cost Price (INR),Selling Price (INR),Stock Cost Value (INR),Total Weight (kg)")
        for (spice in spices) {
            for (packSize in PackSize.entries) {
                val pack = spice.packFor(packSize) ?: continue
                val weightKg = (pack.stockPackets.toLong() * packSize.grams) / 1000.0
                sb.appendLine(
                    listOf(
                        csvEscape(spice.nameEn),
                        csvEscape(spice.nameHi),
                        csvEscape(spice.category.labelEn),
                        csvEscape(packSize.label),
                        pack.stockPackets.toString(),
                        String.format("%.2f", pack.costPrice),
                        String.format("%.2f", pack.sellingPrice),
                        String.format("%.2f", pack.inventoryCostValue),
                        String.format("%.3f", weightKg)
                    ).joinToString(",")
                )
            }
        }

        sb.appendLine()
        sb.appendLine("=== SALES & UDHAAR LEDGER (बिक्री और उधारी खाता) ===")
        sb.appendLine("Invoice No,Date & Time,Customer / Shopkeeper,Items Summary,Total Bill (INR),Paid Amount (INR),Pending Udhaar (INR),Estimated Profit (INR),Payment Status")
        for (sale in sales) {
            val itemsSummary = sale.items.joinToString(" | ") {
                "${it.spiceNameEn} ${it.packSize.label} x${it.quantityPackets}"
            }
            sb.appendLine(
                listOf(
                    csvEscape(sale.invoiceNumber),
                    csvEscape(FormatUtils.formatDateTime(sale.createdAtMillis)),
                    csvEscape(sale.customerName),
                    csvEscape(itemsSummary),
                    String.format("%.2f", sale.totalAmount),
                    String.format("%.2f", sale.paidAmount),
                    String.format("%.2f", sale.pendingUdhaar),
                    String.format("%.2f", sale.estimatedProfit),
                    csvEscape(sale.paymentStatus.labelEn)
                ).joinToString(",")
            )
        }

        return sb.toString()
    }

    fun writeCsvToStream(outputStream: OutputStream, csvContent: String) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(csvContent)
        }
    }

    fun shareCsvFile(context: Context, csvContent: String, fileName: String) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(csvContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Chanouti Masala Report - $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export CSV Report"))
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
