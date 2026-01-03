package com.maternitytracker.data.repository

import com.maternitytracker.data.dao.LabelDao
import com.maternitytracker.data.dao.ShoppingItemDao
import com.maternitytracker.data.entities.ItemLabelCrossRef
import com.maternitytracker.data.entities.ItemWithLabels
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(
    private val shoppingItemDao: ShoppingItemDao,
    private val labelDao: LabelDao
) {
    
    fun getAllItemsWithLabels(): Flow<List<ItemWithLabels>> = shoppingItemDao.getAllItemsWithLabels()
    
    fun getAllLabels(): Flow<List<Label>> = labelDao.getAllLabels()
    
    suspend fun insertItem(item: ShoppingItem, labelIds: List<Long>): Long {
        val itemId = shoppingItemDao.insertItem(item)
        labelIds.forEach { labelId ->
            shoppingItemDao.insertItemLabelCrossRef(ItemLabelCrossRef(itemId, labelId))
        }
        return itemId
    }
    
    suspend fun updateItem(item: ShoppingItem, labelIds: List<Long>) {
        shoppingItemDao.updateItem(item)
        shoppingItemDao.deleteAllLabelsForItem(item.id)
        labelIds.forEach { labelId ->
            shoppingItemDao.insertItemLabelCrossRef(ItemLabelCrossRef(item.id, labelId))
        }
    }
    
    suspend fun deleteItem(item: ShoppingItem) {
        shoppingItemDao.deleteAllLabelsForItem(item.id)
        shoppingItemDao.deleteItem(item)
    }
    
    suspend fun insertLabel(label: Label): Long = labelDao.insertLabel(label)
    
    suspend fun getLabelByName(name: String): Label? = labelDao.getLabelByName(name)
    
    fun getFilteredItems(labelIds: List<Long>, useAndLogic: Boolean): Flow<List<ItemWithLabels>> {
        return if (labelIds.isEmpty()) {
            getAllItemsWithLabels()
        } else if (useAndLogic) {
            shoppingItemDao.getItemsWithAllLabels(labelIds, labelIds.size)
        } else {
            shoppingItemDao.getItemsWithAnyLabels(labelIds)
        }
    }
    
    suspend fun toggleItemPurchased(item: ShoppingItem) {
        shoppingItemDao.updateItem(item.copy(isPurchased = !item.isPurchased))
    }
}