package com.example

import com.example.data.local.DefaultCatalogSeed
import com.example.data.model.CartItem
import com.example.data.model.PackSize
import com.example.data.model.PaymentStatus
import com.example.data.model.SaleItemUi
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpicePackUi
import com.example.data.model.SpiceWithPacks
import com.example.ui.viewmodel.BillingUiState
import com.example.util.ExportAndShareHelper
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `default catalog has exact 13 spices with 4 pack sizes each`() {
        val seeds = DefaultCatalogSeed.buildDefaultSpices()
        assertEquals(13, seeds.size)
        assertEquals("Haldi", seeds[0].nameEn)
        assertEquals("Chhole Masala", seeds[11].nameEn)
        assertEquals("Custom / Extra Masala", seeds[12].nameEn)
        assertTrue(seeds.all { it.packs.size == 4 })
        assertTrue(
            seeds.all { seed ->
                seed.packs.map { it.packSize }.toSet() == PackSize.entries.toSet()
            }
        )
    }

    @Test
    fun `billing state calculates subtotal profit and udhaar balance accurately`() {
        val item1 = CartItem(
            spiceId = 1L,
            spiceNameEn = "Haldi",
            spiceNameHi = "हल्दी",
            packSize = PackSize.G100,
            quantityPackets = 10,
            unitCostPrice = 25.0,
            unitSellingPrice = 36.0,
            availableStockAtAdd = 40
        )
        val item2 = CartItem(
            spiceId = 2L,
            spiceNameEn = "Mirch",
            spiceNameHi = "मिर्च",
            packSize = PackSize.G50,
            quantityPackets = 5,
            unitCostPrice = 16.0,
            unitSellingPrice = 24.0,
            availableStockAtAdd = 30
        )
        val state = BillingUiState(
            customerName = "Gupta Kirana",
            cartItems = listOf(item1, item2),
            paymentStatus = PaymentStatus.PARTIAL,
            partialPaidInput = "300"
        )

        assertEquals(480.0, state.subtotal, 0.01)
        assertEquals(330.0, state.totalCost, 0.01)
        assertEquals(150.0, state.estimatedProfit, 0.01)
        assertEquals(300.0, state.effectivePaid, 0.01)
        assertEquals(180.0, state.balanceUdhaar, 0.01)
    }

    @Test
    fun `whatsapp receipt and csv export include spice and sale details`() {
        val seeds = DefaultCatalogSeed.buildDefaultSpices()
        val spiceList = seeds.mapIndexed { idx, s ->
            SpiceWithPacks(
                id = (idx + 1).toLong(),
                nameEn = s.nameEn,
                nameHi = s.nameHi,
                category = s.category,
                sortOrder = s.sortOrder,
                packs = s.packs.mapIndexed { pIdx, p ->
                    SpicePackUi(
                        id = (idx * 10 + pIdx + 1).toLong(),
                        spiceId = (idx + 1).toLong(),
                        packSize = p.packSize,
                        stockPackets = p.stockPackets,
                        costPrice = p.costPrice,
                        sellingPrice = p.sellingPrice
                    )
                }
            )
        }
        val sale = SaleWithDetails(
            id = 1L,
            invoiceNumber = "CM-1001",
            customerName = "Verma Store",
            totalAmount = 360.0,
            totalCost = 250.0,
            estimatedProfit = 110.0,
            paidAmount = 200.0,
            paymentStatus = PaymentStatus.PARTIAL,
            createdAtMillis = System.currentTimeMillis(),
            items = listOf(
                SaleItemUi(
                    id = 1L,
                    spiceId = 1L,
                    spiceNameEn = "Haldi",
                    spiceNameHi = "हल्दी",
                    packSize = PackSize.G100,
                    quantityPackets = 10,
                    unitCostPrice = 25.0,
                    unitSellingPrice = 36.0,
                    lineTotal = 360.0,
                    lineProfit = 110.0
                )
            ),
            payments = emptyList()
        )

        val receipt = ExportAndShareHelper.buildWhatsAppReceipt(sale)
        assertTrue(receipt.contains("CHANOUTI MASALA"))
        assertTrue(receipt.contains("CM-1001"))
        assertTrue(receipt.contains("Verma Store"))
        assertTrue(receipt.contains("Balance Udhaar"))

        val csv = ExportAndShareHelper.buildCombinedCsvReport(spiceList, listOf(sale))
        assertTrue(csv.contains("Haldi"))
        assertTrue(csv.contains("Takhauri Mirch"))
        assertTrue(csv.contains("Verma Store"))
        assertTrue(FormatUtils.formatKg(spiceList.first().totalWeightKg).endsWith("kg"))
    }
}
