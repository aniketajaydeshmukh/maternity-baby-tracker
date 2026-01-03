package com.maternitytracker.data.entities

import androidx.room.Entity

@Entity(
    tableName = "item_label_cross_ref",
    primaryKeys = ["itemId", "labelId"]
)
data class ItemLabelCrossRef(
    val itemId: Long,
    val labelId: Long
)