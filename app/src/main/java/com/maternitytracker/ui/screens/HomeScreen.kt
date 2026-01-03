package com.maternitytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maternitytracker.data.entities.ItemWithLabels
import com.maternitytracker.data.entities.Label
import com.maternitytracker.ui.components.AddEditItemDialog
import com.maternitytracker.ui.components.ShoppingItemCard
import com.maternitytracker.viewmodel.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<ItemWithLabels>,
    labels: List<Label>,
    filterState: FilterState,
    onAddItem: (String, Int, Double, List<Long>) -> Unit,
    onUpdateItem: (ItemWithLabels, List<Long>) -> Unit,
    onDeleteItem: (ItemWithLabels) -> Unit,
    onTogglePurchased: (ItemWithLabels) -> Unit,
    onAddLabel: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ItemWithLabels?>(null) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with filter info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shopping List",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (filterState.selectedLabels.isNotEmpty()) {
                        Text(
                            text = "Filtered by: ${filterState.selectedLabels.joinToString(", ") { it.name }} (${if (filterState.useAndLogic) "AND" else "OR"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = "${items.size} items • ${items.count { it.item.isPurchased }} purchased",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter items",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Items list
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (filterState.selectedLabels.isNotEmpty()) 
                            "No items match the current filter" 
                        else 
                            "No items yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (filterState.selectedLabels.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items, key = { it.item.id }) { itemWithLabels ->
                    ShoppingItemCard(
                        itemWithLabels = itemWithLabels,
                        onTogglePurchased = { onTogglePurchased(itemWithLabels) },
                        onEdit = { editingItem = itemWithLabels },
                        onDelete = { onDeleteItem(itemWithLabels) }
                    )
                }
            }
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddEditItemDialog(
            isEdit = false,
            availableLabels = labels,
            onDismiss = { showAddDialog = false },
            onSave = { name, quantity, budget, labelIds ->
                onAddItem(name, quantity, budget, labelIds)
                showAddDialog = false
            },
            onAddLabel = onAddLabel
        )
    }
    
    // Edit Item Dialog
    editingItem?.let { item ->
        AddEditItemDialog(
            isEdit = true,
            itemWithLabels = item,
            availableLabels = labels,
            onDismiss = { editingItem = null },
            onSave = { name, quantity, budget, labelIds ->
                val updatedItem = item.copy(
                    item = item.item.copy(
                        name = name,
                        quantity = quantity,
                        budget = budget
                    )
                )
                onUpdateItem(updatedItem, labelIds)
                editingItem = null
            },
            onAddLabel = onAddLabel
        )
    }
}