package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MarketPriceEntity::class, ListingEntity::class, LivestockRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AgricDatabase : RoomDatabase() {
    abstract fun marketPriceDao(): MarketPriceDao
    abstract fun marketplaceListingDao(): MarketplaceListingDao
    abstract fun livestockRecordDao(): LivestockRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AgricDatabase? = null

        fun getDatabase(context: Context): AgricDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgricDatabase::class.java,
                    "agric_master_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
