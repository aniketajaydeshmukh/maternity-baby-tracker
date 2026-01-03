package com.maternitytracker.data.dao

import androidx.room.*
import com.maternitytracker.data.entities.Label
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    
    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<Label>>
    
    @Query("SELECT * FROM labels WHERE id = :labelId")
    suspend fun getLabelById(labelId: Long): Label?
    
    @Insert
    suspend fun insertLabel(label: Label): Long
    
    @Update
    suspend fun updateLabel(label: Label)
    
    @Delete
    suspend fun deleteLabel(label: Label)
    
    @Query("SELECT * FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): Label?
}