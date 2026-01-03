package com.maternitytracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val budget: Double,
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)