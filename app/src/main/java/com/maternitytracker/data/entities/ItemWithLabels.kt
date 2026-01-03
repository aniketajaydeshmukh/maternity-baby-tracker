package com.maternitytracker.data.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ItemWithLabels(
    @Embedded val item: ShoppingItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(ItemLabelCrossRef::class, parentColumn = "itemId", entityColumn = "labelId")
    )
    val labels: List<Label>
)