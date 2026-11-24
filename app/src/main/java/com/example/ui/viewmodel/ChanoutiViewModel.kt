package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChanoutiDatabase
import com.example.data.model.CartItem
import com.example.data.model.DashboardSummary
import com.example.data.model.LedgerFilter
import com.example.data.model.LowStockAlert
import com.example.data.model.PackSize
import com.example.data.model.PaymentStatus
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpiceCategory
import com.example.data.model.SpicePackUi
import com.example.data.model.SpiceWithPacks
import com.example.data.repository.ChanoutiRepository
import com.example.util.ExportAndShareHelper
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab(
    val titleEn: String,
    val titleHi: String
) {
    DASHBOARD("Dashboard", "हिसाब-किताब"),
    SELL("Sell", "माल बेचें"),
    STOCK("Stock", "स्टॉक"),
    LEDGER("Ledger", "बिक्री/उधारी")
}

data class BillingUiState(
    val customerName: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val partialPaidInput: String = "",
    val isSubmitting: Boolean = false
) {
    val subtotal: Double get() = cartItems.sumOf { it.lineTotal }
    val totalCost: Double get() = cartItems.sumOf { it.lineCost }
    val estimatedProfit: Double get() = subtotal - totalCost
    val effectivePaid: Double
        get() = when (paymentStatus) {
            PaymentStatus.PAID -> subtotal
            PaymentStatus.UDHAAR -> 0.0
            PaymentStatus.PARTIAL -> (partialPaidInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, subtotal)
        }
    val balanceUdhaar: Double get() = (subtotal - effectivePaid).coerceAtLeast(0.0)
}

class ChanoutiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChanoutiRepository(ChanoutiDatabase.getInstance(application))

    val spices: StateFlow<List<SpiceWithPacks>> = repository.spicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleWithDetails>> = repository.salesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCustomers: StateFlow<List<String>> = repository.recentCustomersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = combine(spices, sales) { spiceList, saleList ->
        val startOfToday = FormatUtils.startOfTodayMillis()
        val todaySales = saleList.filter { it.createdAtMillis >= startOfToday }
        val udhaarSales = saleList.filter { it.hasPendingUdhaar }

        val lowAlerts = mutableListOf<LowStockAlert>()
        var invCost = 0.0
        var invSell = 0.0
        var totalPkts = 0
        var totalKg = 0.0

        for (spice in spiceList) {
            totalPkts += spice.totalPackets
            totalKg += spice.totalWeightKg
            for (pack in spice.packs) {
                invCost += pack.inventoryCostValue
                invSell += pack.inventorySellingValue
                if (pack.stockPackets < SpicePackUi.LOW_STOCK_THRESHOLD) {
                    lowAlerts.add(
                        LowStockAlert(
                            spiceId = spice.id,
                            spiceNameEn = spice.nameEn,
                            spiceNameHi = spice.nameHi,
                            packSize = pack.packSize,
                            stockPackets = pack.stockPackets,
                            costPrice = pack.costPrice,
                            sellingPrice = pack.sellingPrice
                        )
                    )
                }
            }
        }

        DashboardSummary(
            todayTotalSales = todaySales.sumOf { it.totalAmount },
            todayEstimatedProfit = todaySales.sumOf { it.estimatedProfit },
            todayOrdersCount = todaySales.size,
            totalPendingUdhaar = udhaarSales.sumOf { it.pendingUdhaar },
            pendingUdhaarCount = udhaarSales.size,
            totalInventoryCostValue = invCost,
            totalInventorySellingValue = invSell,
            totalPacketsInStock = totalPkts,
            totalWeightKg = totalKg,
            lowStockAlerts = lowAlerts.sortedBy { it.stockPackets }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _billingState = MutableStateFlow(BillingUiState())
    val billingState: StateFlow<BillingUiState> = _billingState.asStateFlow()

    private val _lastCompletedSale = MutableStateFlow<SaleWithDetails?>(null)
    val lastCompletedSale: StateFlow<SaleWithDetails?> = _lastCompletedSale.asStateFlow()

    private val _ledgerFilter = MutableStateFlow(LedgerFilter.ALL)
    val ledgerFilter: StateFlow<LedgerFilter> = _ledgerFilter.asStateFlow()

    private val _ledgerSearch = MutableStateFlow("")
    val ledgerSearch: StateFlow<String> = _ledgerSearch.asStateFlow()

    private val _stockSearch = MutableStateFlow("")
    val stockSearch: StateFlow<String> = _stockSearch.asStateFlow()

    private val _stockCategoryFilter = MutableStateFlow<SpiceCategory?>(null)
    val stockCategoryFilter: StateFlow<SpiceCategory?> = _stockCategoryFilter.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openUdhaarLedger() {
        _ledgerFilter.value = LedgerFilter.UDHAAR_PENDING
        _currentTab.value = AppTab.LEDGER
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // --- Billing Actions ---

    fun setCustomerName(name: String) {
        _billingState.update { it.copy(customerName = name) }
    }

    fun setPaymentStatus(status: PaymentStatus) {
        _billingState.update {
            it.copy(
                paymentStatus = status,
                partialPaidInput = if (status == PaymentStatus.PARTIAL && it.partialPaidInput.isBlank()) {
                    val half = (it.subtotal / 2.0).toInt()
                    if (half > 0) half.toString() else ""
                } else {
                    it.partialPaidInput
                }
            )
        }
    }

    fun setPartialPaidInput(input: String) {
        val cleaned = input.filter { it.isDigit() || it == '.' }
        _billingState.update { it.copy(partialPaidInput = cleaned) }
    }

    fun addItemToCart(
        spice: SpiceWithPacks,
        pack: SpicePackUi,
        quantity: Int,
        sellingPriceOverride: Double
    ): Boolean {
        if (quantity <= 0) {
            _snackbarMessage.value = "Enter at least 1 packet"
            return false
        }
        val currentInCart = _billingState.value.cartItems
            .firstOrNull { it.spiceId == spice.id && it.packSize == pack.packSize }
            ?.quantityPackets ?: 0

        if (currentInCart + quantity > pack.stockPackets) {
            _snackbarMessage.value = "Only ${pack.stockPackets - currentInCart} more packets available in stock!"
            return false
        }

        _billingState.update { state ->
            val existingIdx = state.cartItems.indexOfFirst {
                it.spiceId == spice.id && it.packSize == pack.packSize
            }
            val updatedList = state.cartItems.toMutableList()
            if (existingIdx >= 0) {
                val existing = updatedList[existingIdx]
                updatedList[existingIdx] = existing.copy(
                    quantityPackets = existing.quantityPackets + quantity,
                    unitSellingPrice = sellingPriceOverride.coerceAtLeast(0.0)
                )
            } else {
                updatedList.add(
                    CartItem(
                        spiceId = spice.id,
                        spiceNameEn = spice.nameEn,
                        spiceNameHi = spice.nameHi,
                        packSize = pack.packSize,
                        quantityPackets = quantity,
                        unitCostPrice = pack.costPrice,
                        unitSellingPrice = sellingPriceOverride.coerceAtLeast(0.0),
                        availableStockAtAdd = pack.stockPackets
                    )
                )
            }
            state.copy(cartItems = updatedList)
        }
        return true
    }

    fun removeCartItem(spiceId: Long, packSize: PackSize) {
        _billingState.update { state ->
            state.copy(
                cartItems = state.cartItems.filterNot {
                    it.spiceId == spiceId && it.packSize == packSize
                }
            )
        }
    }

    fun clearCart() {
        _billingState.update { BillingUiState() }
    }

    fun confirmSale() {
        val state = _billingState.value
        if (state.cartItems.isEmpty() || state.isSubmitting) return

        val paidInput = state.partialPaidInput.toDoubleOrNull() ?: 0.0
        if (state.paymentStatus == PaymentStatus.PARTIAL && paidInput >= state.subtotal) {
            // Treat full payment cleanly
        }

        viewModelScope.launch {
            _billingState.update { it.copy(isSubmitting = true) }
            val result = repository.confirmSale(
                customerName = state.customerName,
                cartItems = state.cartItems,
                paymentStatus = state.paymentStatus,
                amountPaidInput = paidInput
            )
            result.onSuccess { savedSale ->
                _billingState.value = BillingUiState()
                _lastCompletedSale.value = savedSale
                _snackbarMessage.value = "Bill ${savedSale.invoiceNumber} saved!"
            }.onFailure { err ->
                _billingState.update { it.copy(isSubmitting = false) }
                _snackbarMessage.value = err.message ?: "Failed to save sale"
            }
        }
    }

    fun dismissLastCompletedSale() {
        _lastCompletedSale.value = null
    }

    // --- Stock / Production Actions ---

    fun setStockSearch(query: String) {
        _stockSearch.value = query
    }

    fun setStockCategoryFilter(category: SpiceCategory?) {
        _stockCategoryFilter.value = category
    }

    fun addProductionStock(
        spiceId: Long,
        packSize: PackSize,
        addedPackets: Int,
        costPrice: Double,
        sellingPrice: Double
    ) {
        viewModelScope.launch {
            repository.addProductionAndUpdatePricing(
                spiceId = spiceId,
                packSize = packSize,
                addedPackets = addedPackets,
                newCostPrice = costPrice,
                newSellingPrice = sellingPrice
            )
            _snackbarMessage.value = if (addedPackets > 0) {
                "Added +$addedPackets packets (${packSize.label}) to stock"
            } else {
                "Updated ${packSize.label} pricing"
            }
        }
    }

    fun updateSpiceDetails(
        spiceId: Long,
        nameEn: String,
        nameHi: String,
        category: SpiceCategory,
        sortOrder: Int
    ) {
        if (nameEn.isBlank()) {
            _snackbarMessage.value = "Spice name cannot be empty"
            return
        }
        viewModelScope.launch {
            repository.updateSpiceDetails(spiceId, nameEn, nameHi, category, sortOrder)
            _snackbarMessage.value = "Updated $nameEn"
        }
    }

    fun addNewSpice(
        nameEn: String,
        nameHi: String,
        category: SpiceCategory,
        initialPacks: Map<PackSize, Triple<Int, Double, Double>>
    ) {
        if (nameEn.isBlank()) {
            _snackbarMessage.value = "Please enter spice name"
            return
        }
        viewModelScope.launch {
            repository.addNewSpice(nameEn, nameHi, category, initialPacks)
            _snackbarMessage.value = "Added $nameEn to catalog"
        }
    }

    // --- Ledger / Udhaar Actions ---

    fun setLedgerFilter(filter: LedgerFilter) {
        _ledgerFilter.value = filter
    }

    fun setLedgerSearch(query: String) {
        _ledgerSearch.value = query
    }

    fun receiveUdhaarPayment(saleId: Long, amount: Double, note: String) {
        viewModelScope.launch {
            val result = repository.receiveUdhaarPayment(saleId, amount, note)
            result.onSuccess {
                _snackbarMessage.value = "Payment ${FormatUtils.formatRupees(amount)} recorded!"
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Could not record payment"
            }
        }
    }

    // --- Settings / Backup & Reset ---

    fun buildCsvContent(): String {
        return ExportAndShareHelper.buildCombinedCsvReport(spices.value, sales.value)
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetToDefaults()
            _billingState.value = BillingUiState()
            _lastCompletedSale.value = null
            _snackbarMessage.value = "All data reset to default 13 spices"
        }
    }
}
