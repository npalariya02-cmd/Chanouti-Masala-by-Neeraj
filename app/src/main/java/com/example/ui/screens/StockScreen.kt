package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PackSize
import com.example.data.model.SpiceCategory
import com.example.data.model.SpicePackUi
import com.example.data.model.SpiceWithPacks
import com.example.ui.components.CategoryBadge
import com.example.util.FormatUtils

@Composable
fun StockScreen(
    spices: List<SpiceWithPacks>,
    searchQuery: String,
    categoryFilter: SpiceCategory?,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (SpiceCategory?) -> Unit,
    onOpenAddProduction: (spiceId: Long?, packSize: PackSize) -> Unit,
    onOpenEditSpice: (SpiceWithPacks) -> Unit,
    onOpenAddNewSpice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedState = remember { mutableStateMapOf<Long, Boolean>() }

    val filteredSpices = remember(spices, searchQuery, categoryFilter) {
        spices.filter { spice ->
            val matchesSearch = searchQuery.isBlank() ||
                spice.nameEn.contains(searchQuery, ignoreCase = true) ||
                spice.nameHi.contains(searchQuery, ignoreCase = true)
            val matchesCat = categoryFilter == null || spice.category == categoryFilter
            matchesSearch && matchesCat
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenAddProduction(spices.firstOrNull()?.id, PackSize.G100) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("add_production_main_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Production / Stock", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                OutlinedButton(
                    onClick = onOpenAddNewSpice,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(50.dp)
                        .testTag("add_new_spice_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Spice", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // Search & Category Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search spice (मसाला खोजें)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoryFilter == null,
                        onClick = { onCategoryFilterChange(null) },
                        label = { Text("All (${spices.size})") }
                    )
                    SpiceCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = categoryFilter == cat,
                            onClick = {
                                onCategoryFilterChange(if (categoryFilter == cat) null else cat)
                            },
                            label = { Text(cat.labelEn) }
                        )
                    }
                }
            }
        }

        items(filteredSpices, key = { it.id }) { spice ->
            val isExpanded = expandedState[spice.id] ?: false
            ExpandableSpiceCard(
                spice = spice,
                isExpanded = isExpanded,
                onToggleExpand = { expandedState[spice.id] = !isExpanded },
                onEditSpice = { onOpenEditSpice(spice) },
                onPackAction = { packSize -> onOpenAddProduction(spice.id, packSize) }
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun ExpandableSpiceCard(
    spice: SpiceWithPacks,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditSpice: () -> Unit,
    onPackAction: (PackSize) -> Unit
) {
    val lowCount = spice.lowStockPackCount
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (lowCount > 0) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CategoryBadge(category = spice.category)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${spice.nameHi} • ${spice.totalPackets} pkts (${FormatUtils.formatKg(spice.totalWeightKg)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lowCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$lowCount pack size(s) < 10 pkts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditSpice) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit ${spice.nameEn}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded Pack Size Breakdown (10g, 50g, 100g, 200g)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    PackSize.entries.forEach { size ->
                        val pack = spice.packFor(size)
                        if (pack != null) {
                            PackRowItem(
                                pack = pack,
                                onAddOrEdit = { onPackAction(size) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PackRowItem(
    pack: SpicePackUi,
    onAddOrEdit: () -> Unit
) {
    val isLow = pack.isLowStock
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isLow) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isLow) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pack.packSize.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pack.stockPackets} pkts",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Cost ${FormatUtils.formatRupees(pack.costPrice)}  •  Sell ${FormatUtils.formatRupees(pack.sellingPrice)}  •  Profit ${FormatUtils.formatRupees(pack.profitPerPacket)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onAddOrEdit,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Stock/Rate", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
