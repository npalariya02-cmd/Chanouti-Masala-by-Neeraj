package com.example.data.model

enum class PackSize(val grams: Int, val label: String) {
    G10(10, "10g"),
    G50(50, "50g"),
    G100(100, "100g"),
    G200(200, "200g");

    companion object {
        fun fromGrams(grams: Int): PackSize = entries.firstOrNull { it.grams == grams } ?: G100
    }
}

enum class SpiceCategory(val labelEn: String, val labelHi: String) {
    BASIC("Basic", "सादा"),
    BLENDED("Blended", "मिक्स"),
    SPECIAL("Special", "स्पेशल"),
    WHOLE_SPECIAL("Whole/Special", "खड़ा/स्पेशल"),
    OTHER("Other", "अन्य");

    companion object {
        fun fromLabel(label: String): SpiceCategory =
            entries.firstOrNull { it.labelEn.equals(label, ignoreCase = true) } ?: OTHER
    }
}

enum class PaymentStatus(val labelEn: String, val labelHi: String) {
    PAID("Paid", "नकद/UPI"),
    PARTIAL("Partial", "आधा मिला"),
    UDHAAR("Udhaar", "उधारी");

    companion object {
        fun fromString(value: String): PaymentStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PAID
    }
}

enum class LedgerFilter(val labelEn: String, val labelHi: String) {
    ALL("All", "सभी"),
    TODAY("Today", "आज"),
    UDHAAR_PENDING("Udhaar Pending", "उधारी बाकी")
}

data class SpicePackUi(
    val id: Long,
    val spiceId: Long,
    val packSize: PackSize,
    val stockPackets: Int,
    val costPrice: Double,
    val sellingPrice: Double
) {
    val profitPerPacket: Double get() = sellingPrice - costPrice
    val inventoryCostValue: Double get() = stockPackets * costPrice
    val inventorySellingValue: Double get() = stockPackets * sellingPrice
    val totalWeightGrams: Long get() = stockPackets.toLong() * packSize.grams
    val isLowStock: Boolean get() = stockPackets < LOW_STOCK_THRESHOLD

    companion object {
        const val LOW_STOCK_THRESHOLD = 10
    }
}

data class SpiceWithPacks(
    val id: Long,
    val nameEn: String,
    val nameHi: String,
    val category: SpiceCategory,
    val sortOrder: Int,
    val packs: List<SpicePackUi>
) {
    val totalPackets: Int get() = packs.sumOf { it.stockPackets }
    val totalWeightKg: Double get() = packs.sumOf { it.totalWeightGrams } / 1000.0
    val totalCostValue: Double get() = packs.sumOf { it.inventoryCostValue }
    val lowStockPackCount: Int get() = packs.count { it.isLowStock }

    fun packFor(size: PackSize): SpicePackUi? = packs.firstOrNull { it.packSize == size }
}

data class CartItem(
    val spiceId: Long,
    val spiceNameEn: String,
    val spiceNameHi: String,
    val packSize: PackSize,
    val quantityPackets: Int,
    val unitCostPrice: Double,
    val unitSellingPrice: Double,
    val availableStockAtAdd: Int
) {
    val lineTotal: Double get() = quantityPackets * unitSellingPrice
    val lineCost: Double get() = quantityPackets * unitCostPrice
    val lineProfit: Double get() = lineTotal - lineCost
}

data class SaleItemUi(
    val id: Long,
    val spiceId: Long,
    val spiceNameEn: String,
    val spiceNameHi: String,
    val packSize: PackSize,
    val quantityPackets: Int,
    val unitCostPrice: Double,
    val unitSellingPrice: Double,
    val lineTotal: Double,
    val lineProfit: Double
)

data class PaymentRecordUi(
    val id: Long,
    val amountReceived: Double,
    val note: String,
    val paidAtMillis: Long
)

data class SaleWithDetails(
    val id: Long,
    val invoiceNumber: String,
    val customerName: String,
    val totalAmount: Double,
    val totalCost: Double,
    val estimatedProfit: Double,
    val paidAmount: Double,
    val paymentStatus: PaymentStatus,
    val createdAtMillis: Long,
    val items: List<SaleItemUi>,
    val payments: List<PaymentRecordUi>
) {
    val pendingUdhaar: Double get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
    val hasPendingUdhaar: Boolean get() = pendingUdhaar > 0.01
}

data class LowStockAlert(
    val spiceId: Long,
    val spiceNameEn: String,
    val spiceNameHi: String,
    val packSize: PackSize,
    val stockPackets: Int,
    val costPrice: Double,
    val sellingPrice: Double
)

data class DashboardSummary(
    val todayTotalSales: Double = 0.0,
    val todayEstimatedProfit: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val totalPendingUdhaar: Double = 0.0,
    val pendingUdhaarCount: Int = 0,
    val totalInventoryCostValue: Double = 0.0,
    val totalInventorySellingValue: Double = 0.0,
    val totalPacketsInStock: Int = 0,
    val totalWeightKg: Double = 0.0,
    val lowStockAlerts: List<LowStockAlert> = emptyList()
)
