package com.maternitytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maternitytracker.data.entities.ItemWithLabels
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import com.maternitytracker.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class FilterState(
    val selectedLabels: List<Label> = emptyList(),
    val useAndLogic: Boolean = false
)

data class BudgetSummary(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val remaining: Double = 0.0,
    val labelBreakdown: Map<String, Pair<Double, Double>> = emptyMap() // label -> (spent, budget)
)

class ShoppingViewModel(private val repository: ShoppingRepository) : ViewModel() {
    
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _budgetSummary = MutableStateFlow(BudgetSummary())
    val budgetSummary: StateFlow<BudgetSummary> = _budgetSummary.asStateFlow()
    
    val allLabels = repository.getAllLabels()
    
    val filteredItems = _filterState.flatMapLatest { filter ->
        val labelIds = filter.selectedLabels.map { it.id }
        repository.getFilteredItems(labelIds, filter.useAndLogic)
    }
    
    init {
        // Calculate budget summary whenever items change
        viewModelScope.launch {
            repository.getAllItemsWithLabels().collect { items ->
                calculateBudgetSummary(items)
            }
        }
    }
    
    private fun calculateBudgetSummary(items: List<ItemWithLabels>) {
        val totalBudget = items.sumOf { it.item.budget }
        val totalSpent = items.filter { it.item.isPurchased }.sumOf { it.item.budget }
        val remaining = totalBudget - totalSpent
        
        val labelBreakdown = mutableMapOf<String, Pair<Double, Double>>()
        
        items.forEach { itemWithLabels ->
            itemWithLabels.labels.forEach { label ->
                val current = labelBreakdown[label.name] ?: (0.0 to 0.0)
                val spent = if (itemWithLabels.item.isPurchased) itemWithLabels.item.budget else 0.0
                labelBreakdown[label.name] = (current.first + spent) to (current.second + itemWithLabels.item.budget)
            }
        }
        
        _budgetSummary.value = BudgetSummary(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            labelBreakdown = labelBreakdown
        )
    }
    
    fun addItem(name: String, quantity: Int, budget: Double, labelIds: List<Long>) {
        viewModelScope.launch {
            val item = ShoppingItem(
                name = name,
                quantity = quantity,
                budget = budget
            )
            repository.insertItem(item, labelIds)
        }
    }
    
    fun updateItem(item: ShoppingItem, labelIds: List<Long>) {
        viewModelScope.launch {
            repository.updateItem(item, labelIds)
        }
    }
    
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
    
    fun toggleItemPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleItemPurchased(item)
        }
    }
    
    fun addLabel(name: String) {
        viewModelScope.launch {
            val existingLabel = repository.getLabelByName(name)
            if (existingLabel == null) {
                repository.insertLabel(Label(name = name))
            }
        }
    }
    
    fun updateFilter(selectedLabels: List<Label>, useAndLogic: Boolean) {
        _filterState.value = FilterState(selectedLabels, useAndLogic)
    }
    
    fun clearFilter() {
        _filterState.value = FilterState()
    }
}