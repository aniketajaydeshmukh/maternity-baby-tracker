package com.maternitytracker.data.dao

import androidx.room.*
import com.maternitytracker.data.entities.ItemLabelCrossRef
import com.maternitytracker.data.entities.ItemWithLabels
import com.maternitytracker.data.entities.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    
    @Transaction
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, createdAt DESC")
    fun getAllItemsWithLabels(): Flow<List<ItemWithLabels>>
    
    @Transaction
    @Query("SELECT * FROM shopping_items WHERE id = :itemId")
    suspend fun getItemWithLabels(itemId: Long): ItemWithLabels?
    
    @Insert
    suspend fun insertItem(item: ShoppingItem): Long
    
    @Update
    suspend fun updateItem(item: ShoppingItem)
    
    @Delete
    suspend fun deleteItem(item: ShoppingItem)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemLabelCrossRef(crossRef: ItemLabelCrossRef)
    
    @Delete
    suspend fun deleteItemLabelCrossRef(crossRef: ItemLabelCrossRef)
    
    @Query("DELETE FROM item_label_cross_ref WHERE itemId = :itemId")
    suspend fun deleteAllLabelsForItem(itemId: Long)
    
    @Transaction
    @Query("""
        SELECT DISTINCT si.* FROM shopping_items si
        INNER JOIN item_label_cross_ref ilcr ON si.id = ilcr.itemId
        WHERE ilcr.labelId IN (:labelIds)
        GROUP BY si.id
        HAVING COUNT(DISTINCT ilcr.labelId) = :labelCount
        ORDER BY si.isPurchased ASC, si.createdAt DESC
    """)
    fun getItemsWithAllLabels(labelIds: List<Long>, labelCount: Int): Flow<List<ItemWithLabels>>
    
    @Transaction
    @Query("""
        SELECT DISTINCT si.* FROM shopping_items si
        INNER JOIN item_label_cross_ref ilcr ON si.id = ilcr.itemId
        WHERE ilcr.labelId IN (:labelIds)
        ORDER BY si.isPurchased ASC, si.createdAt DESC
    """)
    fun getItemsWithAnyLabels(labelIds: List<Long>): Flow<List<ItemWithLabels>>
}