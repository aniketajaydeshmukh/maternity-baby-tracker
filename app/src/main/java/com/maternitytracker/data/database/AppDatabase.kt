package com.maternitytracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.maternitytracker.data.dao.LabelDao
import com.maternitytracker.data.dao.ShoppingItemDao
import com.maternitytracker.data.entities.ItemLabelCrossRef
import com.maternitytracker.data.entities.Label
import com.maternitytracker.data.entities.ShoppingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ShoppingItem::class, Label::class, ItemLabelCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun labelDao(): LabelDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maternity_tracker_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.labelDao())
                    }
                }
            }
        }
        
        private suspend fun populateDatabase(labelDao: LabelDao) {
            // Insert default labels
            val defaultLabels = listOf(
                Label(name = "Baby Clothes", color = "#E1BEE7"),
                Label(name = "Maternity Essentials", color = "#F8BBD9"),
                Label(name = "Nursery", color = "#E8F5E8"),
                Label(name = "Health & Safety", color = "#FFE0B2")
            )
            
            defaultLabels.forEach { label ->
                labelDao.insertLabel(label)
            }
        }
    }
}