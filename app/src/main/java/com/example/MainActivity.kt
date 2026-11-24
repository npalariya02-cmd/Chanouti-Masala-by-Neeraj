package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PackSize
import com.example.data.model.SaleWithDetails
import com.example.data.model.SpiceWithPacks
import com.example.ui.components.AddNewSpiceDialog
import com.example.ui.components.AddProductionDialog
import com.example.ui.components.EditSpiceDialog
import com.example.ui.components.ReceivePaymentDialog
import com.example.ui.components.SaleReceiptDialog
import com.example.ui.components.SettingsAndBackupDialog
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.ChanoutiMasalaTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ChanoutiViewModel
import com.example.util.ExportAndShareHelper
import com.example.util.FormatUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChanoutiMasalaTheme {
                ChanoutiMasalaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChanoutiMasalaApp(
    viewModel: ChanoutiViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val spices by viewModel.spices.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val recentCustomers by viewModel.recentCustomers.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsStateWithLifecycle()
    val ledgerFilter by viewModel.ledgerFilter.collectAsStateWithLifecycle()
    val ledgerSearch by viewModel.ledgerSearch.collectAsStateWithLifecycle()
    val stockSearch by viewModel.stockSearch.collectAsStateWithLifecycle()
    val stockCategoryFilter by viewModel.stockCategoryFilter.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Modal state holders
    var productionTarget by remember { mutableStateOf<Pair<Long?, PackSize>?>(null) }
    var editingSpice by remember { mutableStateOf<SpiceWithPacks?>(null) }
    var showAddNewSpice by remember { mutableStateOf(false) }
    var paymentTargetSale by remember { mutableStateOf<SaleWithDetails?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val csvCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = viewModel.buildCsvContent()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    ExportAndShareHelper.writeCsvToStream(out, csvContent)
                }
                viewModel.showMessage("CSV backup saved successfully!")
            } catch (e: Exception) {
                viewModel.showMessage("Could not save CSV: ${e.message}")
            }
        }
    }

    val triggerSaveCsv: () -> Unit = {
        val fileName = "Chanouti_Masala_Report_${FormatUtils.formatFileTimestamp()}.csv"
        csvCreateLauncher.launch(fileName)
    }

    val triggerShareCsv: () -> Unit = {
        try {
            val fileName = "Chanouti_Masala_Report_${FormatUtils.formatFileTimestamp()}.csv"
            val csvContent = viewModel.buildCsvContent()
            ExportAndShareHelper.shareCsvFile(context, csvContent, fileName)
        } catch (e: Exception) {
            viewModel.showMessage("Could not share CSV: ${e.message}")
        }
    }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Chanouti Masala • ${currentTab.titleEn}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "चुनौती मसाला • ${currentTab.titleHi}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings and CSV Backup"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        cartCount = billingState.cartItems.size
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.titleEn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    TabIconWithBadge(
                                        tab = tab,
                                        selected = selected,
                                        cartCount = billingState.cartItems.size
                                    )
                                },
                                label = { Text(tab.titleEn) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        when (currentTab) {
                            AppTab.DASHBOARD -> DashboardScreen(
                                summary = dashboardSummary,
                                spices = spices,
                                onGoToBilling = { viewModel.selectTab(AppTab.SELL) },
                                onGoToUdhaarLedger = { viewModel.openUdhaarLedger() },
                                onQuickRestock = { spiceId, packSize ->
                                    productionTarget = spiceId to packSize
                                },
                                onOpenSpiceInStock = { viewModel.selectTab(AppTab.STOCK) }
                            )

                            AppTab.SELL -> BillingScreen(
                                spices = spices,
                                recentCustomers = recentCustomers,
                                billingState = billingState,
                                onCustomerNameChange = viewModel::setCustomerName,
                                onAddItemToCart = { spice, packSize, qty, price ->
                                    val pack = spice.packFor(packSize)
                                    if (pack != null) {
                                        viewModel.addItemToCart(spice, pack, qty, price)
                                    } else {
                                        false
                                    }
                                },
                                onRemoveCartItem = viewModel::removeCartItem,
                                onClearCart = viewModel::clearCart,
                                onPaymentStatusChange = viewModel::setPaymentStatus,
                                onPartialPaidChange = viewModel::setPartialPaidInput,
                                onConfirmSale = viewModel::confirmSale
                            )

                            AppTab.STOCK -> StockScreen(
                                spices = spices,
                                searchQuery = stockSearch,
                                categoryFilter = stockCategoryFilter,
                                onSearchChange = viewModel::setStockSearch,
                                onCategoryFilterChange = viewModel::setStockCategoryFilter,
                                onOpenAddProduction = { spiceId, packSize ->
                                    productionTarget = spiceId to packSize
                                },
                                onOpenEditSpice = { spice -> editingSpice = spice },
                                onOpenAddNewSpice = { showAddNewSpice = true }
                            )

                            AppTab.LEDGER -> LedgerScreen(
                                sales = sales,
                                filter = ledgerFilter,
                                searchQuery = ledgerSearch,
                                onFilterChange = viewModel::setLedgerFilter,
                                onSearchChange = viewModel::setLedgerSearch,
                                onReceivePayment = { sale -> paymentTargetSale = sale },
                                onShareWhatsApp = { sale ->
                                    ExportAndShareHelper.shareReceiptOnWhatsApp(context, sale)
                                },
                                onExportCsv = triggerSaveCsv
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Modals & Dialogs ---

    productionTarget?.let { (spiceId, packSize) ->
        AddProductionDialog(
            spices = spices,
            initialSpiceId = spiceId,
            initialPackSize = packSize,
            onDismiss = { productionTarget = null },
            onSave = { sId, pSize, added, cost, sell ->
                viewModel.addProductionStock(sId, pSize, added, cost, sell)
                productionTarget = null
            }
        )
    }

    editingSpice?.let { spice ->
        EditSpiceDialog(
            spice = spice,
            onDismiss = { editingSpice = null },
            onSave = { nameEn, nameHi, category ->
                viewModel.updateSpiceDetails(spice.id, nameEn, nameHi, category, spice.sortOrder)
                editingSpice = null
            }
        )
    }

    if (showAddNewSpice) {
        AddNewSpiceDialog(
            onDismiss = { showAddNewSpice = false },
            onAdd = { nameEn, nameHi, category, packs ->
                viewModel.addNewSpice(nameEn, nameHi, category, packs)
                showAddNewSpice = false
            }
        )
    }

    lastCompletedSale?.let { completedSale ->
        SaleReceiptDialog(
            sale = completedSale,
            onDismiss = { viewModel.dismissLastCompletedSale() },
            onShareWhatsApp = { sale ->
                ExportAndShareHelper.shareReceiptOnWhatsApp(context, sale)
            }
        )
    }

    paymentTargetSale?.let { sale ->
        ReceivePaymentDialog(
            sale = sale,
            onDismiss = { paymentTargetSale = null },
            onConfirmPayment = { amount, note ->
                viewModel.receiveUdhaarPayment(sale.id, amount, note)
                paymentTargetSale = null
            }
        )
    }

    if (showSettingsDialog) {
        SettingsAndBackupDialog(
            onDismiss = { showSettingsDialog = false },
            onSaveCsvToDevice = {
                showSettingsDialog = false
                triggerSaveCsv()
            },
            onShareCsv = {
                showSettingsDialog = false
                triggerShareCsv()
            },
            onResetAllData = {
                viewModel.resetAllData()
            }
        )
    }
}

@Composable
private fun TabIconWithBadge(
    tab: AppTab,
    selected: Boolean,
    cartCount: Int
) {
    val icon = when (tab) {
        AppTab.DASHBOARD -> if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
        AppTab.SELL -> if (selected) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale
        AppTab.STOCK -> if (selected) Icons.Filled.Inventory else Icons.Outlined.Inventory
        AppTab.LEDGER -> if (selected) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong
    }

    if (tab == AppTab.SELL && cartCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(cartCount.toString())
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = tab.titleEn)
        }
    } else {
        Icon(imageVector = icon, contentDescription = tab.titleEn)
    }
}
