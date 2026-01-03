package com.maternitytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.maternitytracker.data.entities.ItemWithLabels
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemDialog(
    isEdit: Boolean = false,
    itemWithLabels: ItemWithLabels? = null,
    availableLabels: List<Label>,
    onDismiss: () -> Unit,
    onSave: (String, Int, Double, List<Long>) -> Unit,
    onAddLabel: (String) -> Unit
) {
    var name by remember { mutableStateOf(itemWithLabels?.item?.name ?: "") }
    var quantity by remember { mutableStateOf(itemWithLabels?.item?.quantity?.toString() ?: "1") }
    var budget by remember { mutableStateOf(itemWithLabels?.item?.budget?.toString() ?: "") }
    var selectedLabels by remember { 
        mutableStateOf(itemWithLabels?.labels?.map { it.id } ?: emptyList()) 
    }
    var showAddLabelDialog by remember { mutableStateOf(false) }
    var newLabelName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Item" else "Add New Item",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Quantity field
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Budget field
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Budget (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Labels section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Labels",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    IconButton(onClick = { showAddLabelDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new label",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Labels list
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(availableLabels) { label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedLabels.contains(label.id),
                                onCheckedChange = { isChecked ->
                                    selectedLabels = if (isChecked) {
                                        selectedLabels + label.id
                                    } else {
                                        selectedLabels - label.id
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            LabelChip(
                                label = label.name,
                                color = Color(android.graphics.Color.parseColor(label.color))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val quantityInt = quantity.toIntOrNull() ?: 1
                            val budgetDouble = budget.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onSave(name, quantityInt, budgetDouble, selectedLabels)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text(if (isEdit) "Update" else "Add")
                    }
                }
            }
        }
    }
    
    // Add Label Dialog
    if (showAddLabelDialog) {
        Dialog(onDismissRequest = { showAddLabelDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Add New Label",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newLabelName,
                        onValueChange = { newLabelName = it },
                        label = { Text("Label Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { 
                            showAddLabelDialog = false
                            newLabelName = ""
                        }) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (newLabelName.isNotBlank()) {
                                    onAddLabel(newLabelName)
                                    showAddLabelDialog = false
                                    newLabelName = ""
                                }
                            },
                            enabled = newLabelName.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}