package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.ChanoutiDatabase
import com.example.data.local.PaymentRecordEntity
import com.example.data.local.SaleEntity
import com.example.data.local.SaleItemEntity
import com.example.data.local.SpiceEntity
import com.example.data.local.SpicePackEntity
import com.example.data.model.CartItem
import com.example.data.model.PackSize
import com.example.data.model.PaymentRecordUi
import com.example.data.model.PaymentStatus
import com.example.data.model.SaleItemUi
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpiceCategory
import com.example.data.model.SpicePackUi
import com.example.data.model.SpiceWithPacks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class ChanoutiRepository(private val database: ChanoutiDatabase) {
    private val dao = database.chanoutiDao()

    val spicesFlow: Flow<List<SpiceWithPacks>> = dao.observeSpicesWithPacks().map { list ->
        list.map { rel ->
            SpiceWithPacks(
                id = rel.spice.id,
                nameEn = rel.spice.nameEn,
                nameHi = rel.spice.nameHi,
                category = SpiceCategory.fromLabel(rel.spice.category),
                sortOrder = rel.spice.sortOrder,
                packs = rel.packs
                    .sortedBy { it.packGrams }
                    .map { pack ->
                        SpicePackUi(
                            id = pack.id,
                            spiceId = pack.spiceId,
                            packSize = PackSize.fromGrams(pack.packGrams),
                            stockPackets = pack.stockPackets,
                            costPrice = pack.costPrice,
                            sellingPrice = pack.sellingPrice
                        )
                    }
            )
        }
    }

    val salesFlow: Flow<List<SaleWithDetails>> = dao.observeSalesWithDetails().map { list ->
        list.map { rel ->
            SaleWithDetails(
                id = rel.sale.id,
                invoiceNumber = rel.sale.invoiceNumber,
                customerName = rel.sale.customerName,
                totalAmount = rel.sale.totalAmount,
                totalCost = rel.sale.totalCost,
                estimatedProfit = rel.sale.estimatedProfit,
                paidAmount = rel.sale.paidAmount,
                paymentStatus = PaymentStatus.fromString(rel.sale.paymentStatus),
                createdAtMillis = rel.sale.createdAtMillis,
                items = rel.items.map { item ->
                    SaleItemUi(
                        id = item.id,
                        spiceId = item.spiceId,
                        spiceNameEn = item.spiceNameEn,
                        spiceNameHi = item.spiceNameHi,
                        packSize = PackSize.fromGrams(item.packGrams),
                        quantityPackets = item.quantityPackets,
                        unitCostPrice = item.unitCostPrice,
                        unitSellingPrice = item.unitSellingPrice,
                        lineTotal = item.lineTotal,
                        lineProfit = item.lineProfit
                    )
                },
                payments = rel.payments
                    .sortedByDescending { it.paidAtMillis }
                    .map { p ->
                        PaymentRecordUi(
                            id = p.id,
                            amountReceived = p.amountReceived,
                            note = p.note,
                            paidAtMillis = p.paidAtMillis
                        )
                    }
            )
        }
    }

    val recentCustomersFlow: Flow<List<String>> = dao.observeRecentCustomers()

    suspend fun ensureSeeded() {
        dao.seedCatalogIfEmpty()
    }

    suspend fun resetToDefaults() {
        dao.resetAllAndReseed()
    }

    suspend fun updateSpiceDetails(
        spiceId: Long,
        nameEn: String,
        nameHi: String,
        category: SpiceCategory,
        sortOrder: Int
    ) {
        dao.updateSpice(
            SpiceEntity(
                id = spiceId,
                nameEn = nameEn.trim(),
                nameHi = nameHi.trim(),
                category = category.labelEn,
                sortOrder = sortOrder
            )
        )
    }

    suspend fun addNewSpice(
        nameEn: String,
        nameHi: String,
        category: SpiceCategory,
        initialPacks: Map<PackSize, Triple<Int, Double, Double>>
    ) {
        database.withTransaction {
            val nextOrder = (dao.getMaxSortOrder() ?: 0) + 1
            val spiceId = dao.insertSpice(
                SpiceEntity(
                    nameEn = nameEn.trim(),
                    nameHi = nameHi.trim(),
                    category = category.labelEn,
                    sortOrder = nextOrder
                )
            )
            val packEntities = PackSize.entries.map { size ->
                val (stock, cost, sell) = initialPacks[size] ?: Triple(0, 0.0, 0.0)
                SpicePackEntity(
                    spiceId = spiceId,
                    packGrams = size.grams,
                    stockPackets = stock.coerceAtLeast(0),
                    costPrice = cost.coerceAtLeast(0.0),
                    sellingPrice = sell.coerceAtLeast(0.0)
                )
            }
            dao.insertPacks(packEntities)
        }
    }

    suspend fun addProductionAndUpdatePricing(
        spiceId: Long,
        packSize: PackSize,
        addedPackets: Int,
        newCostPrice: Double,
        newSellingPrice: Double
    ) {
        database.withTransaction {
            val existing = dao.getPack(spiceId, packSize.grams)
            if (existing != null) {
                dao.updatePack(
                    existing.copy(
                        stockPackets = (existing.stockPackets + addedPackets).coerceAtLeast(0),
                        costPrice = newCostPrice.coerceAtLeast(0.0),
                        sellingPrice = newSellingPrice.coerceAtLeast(0.0)
                    )
                )
            } else {
                dao.insertPacks(
                    listOf(
                        SpicePackEntity(
                            spiceId = spiceId,
                            packGrams = packSize.grams,
                            stockPackets = addedPackets.coerceAtLeast(0),
                            costPrice = newCostPrice.coerceAtLeast(0.0),
                            sellingPrice = newSellingPrice.coerceAtLeast(0.0)
                        )
                    )
                )
            }
        }
    }

    suspend fun confirmSale(
        customerName: String,
        cartItems: List<CartItem>,
        paymentStatus: PaymentStatus,
        amountPaidInput: Double
    ): Result<SaleWithDetails> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        return try {
            val savedSale = database.withTransaction {
                // Verify and deduct stock for every line item
                for (item in cartItems) {
                    val pack = dao.getPack(item.spiceId, item.packSize.grams)
                        ?: throw IllegalStateException("${item.spiceNameEn} ${item.packSize.label} not found")
                    if (pack.stockPackets < item.quantityPackets) {
                        throw IllegalStateException(
                            "Insufficient stock for ${item.spiceNameEn} (${item.packSize.label}). Available: ${pack.stockPackets}"
                        )
                    }
                    dao.updatePack(
                        pack.copy(stockPackets = pack.stockPackets - item.quantityPackets)
                    )
                }

                val now = System.currentTimeMillis()
                val totalAmount = cartItems.sumOf { it.lineTotal }
                val totalCost = cartItems.sumOf { it.lineCost }
                val estimatedProfit = totalAmount - totalCost

                val finalPaidAmount = when (paymentStatus) {
                    PaymentStatus.PAID -> totalAmount
                    PaymentStatus.UDHAAR -> 0.0
                    PaymentStatus.PARTIAL -> amountPaidInput.coerceIn(0.0, totalAmount)
                }

                val resolvedStatus = when {
                    finalPaidAmount >= totalAmount - 0.01 -> PaymentStatus.PAID
                    finalPaidAmount <= 0.01 -> PaymentStatus.UDHAAR
                    else -> PaymentStatus.PARTIAL
                }

                val nextNumber = 1001 + dao.countSales()
                val invoiceNumber = String.format(Locale.US, "CM-%04d", nextNumber)
                val cleanCustomer = customerName.trim().ifBlank { "Cash Customer" }

                val saleId = dao.insertSale(
                    SaleEntity(
                        invoiceNumber = invoiceNumber,
                        customerName = cleanCustomer,
                        totalAmount = totalAmount,
                        totalCost = totalCost,
                        estimatedProfit = estimatedProfit,
                        paidAmount = finalPaidAmount,
                        paymentStatus = resolvedStatus.name,
                        createdAtMillis = now
                    )
                )

                val itemEntities = cartItems.map { item ->
                    SaleItemEntity(
                        saleId = saleId,
                        spiceId = item.spiceId,
                        spiceNameEn = item.spiceNameEn,
                        spiceNameHi = item.spiceNameHi,
                        packGrams = item.packSize.grams,
                        quantityPackets = item.quantityPackets,
                        unitCostPrice = item.unitCostPrice,
                        unitSellingPrice = item.unitSellingPrice,
                        lineTotal = item.lineTotal,
                        lineProfit = item.lineProfit
                    )
                }
                dao.insertSaleItems(itemEntities)

                val paymentRecords = if (finalPaidAmount > 0.0) {
                    val recId = dao.insertPaymentRecord(
                        PaymentRecordEntity(
                            saleId = saleId,
                            amountReceived = finalPaidAmount,
                            note = if (resolvedStatus == PaymentStatus.PAID) "Full Payment" else "Initial Payment",
                            paidAtMillis = now
                        )
                    )
                    listOf(
                        PaymentRecordUi(
                            id = recId,
                            amountReceived = finalPaidAmount,
                            note = if (resolvedStatus == PaymentStatus.PAID) "Full Payment" else "Initial Payment",
                            paidAtMillis = now
                        )
                    )
                } else {
                    emptyList()
                }

                SaleWithDetails(
                    id = saleId,
                    invoiceNumber = invoiceNumber,
                    customerName = cleanCustomer,
                    totalAmount = totalAmount,
                    totalCost = totalCost,
                    estimatedProfit = estimatedProfit,
                    paidAmount = finalPaidAmount,
                    paymentStatus = resolvedStatus,
                    createdAtMillis = now,
                    items = itemEntities.map {
                        SaleItemUi(
                            id = it.id,
                            spiceId = it.spiceId,
                            spiceNameEn = it.spiceNameEn,
                            spiceNameHi = it.spiceNameHi,
                            packSize = PackSize.fromGrams(it.packGrams),
                            quantityPackets = it.quantityPackets,
                            unitCostPrice = it.unitCostPrice,
                            unitSellingPrice = it.unitSellingPrice,
                            lineTotal = it.lineTotal,
                            lineProfit = it.lineProfit
                        )
                    },
                    payments = paymentRecords
                )
            }
            Result.success(savedSale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun receiveUdhaarPayment(
        saleId: Long,
        amountReceived: Double,
        note: String
    ): Result<Unit> {
        if (amountReceived <= 0.0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        return try {
            database.withTransaction {
                val sale = dao.getSaleById(saleId)
                    ?: throw IllegalStateException("Sale record not found")
                val remaining = (sale.totalAmount - sale.paidAmount).coerceAtLeast(0.0)
                val applied = amountReceived.coerceAtMost(remaining)
                val newPaid = (sale.paidAmount + applied).coerceAtMost(sale.totalAmount)
                val newStatus = if (newPaid >= sale.totalAmount - 0.01) {
                    PaymentStatus.PAID
                } else {
                    PaymentStatus.PARTIAL
                }
                dao.updateSale(
                    sale.copy(
                        paidAmount = newPaid,
                        paymentStatus = newStatus.name
                    )
                )
                dao.insertPaymentRecord(
                    PaymentRecordEntity(
                        saleId = saleId,
                        amountReceived = applied,
                        note = note.trim().ifBlank { "Udhaar Settlement" },
                        paidAtMillis = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
