package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketPriceDao {
    @Query("SELECT * FROM market_prices ORDER BY commodity ASC")
    fun getAllPrices(): Flow<List<MarketPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<MarketPriceEntity>)

    @Query("DELETE FROM market_prices")
    suspend fun clearPrices()
}

@Dao
interface MarketplaceListingDao {
    @Query("SELECT * FROM marketplace_listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Delete
    suspend fun deleteListing(listing: ListingEntity)

    @Query("DELETE FROM marketplace_listings WHERE id = :id")
    suspend fun deleteListingById(id: Int)
}
