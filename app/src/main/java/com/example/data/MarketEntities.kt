package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_prices")
data class MarketPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val commodity: String,
    val marketName: String,
    val region: String,
    val pricePerUnit: Double,
    val unitName: String,
    val trend: String, // "UP", "DOWN", "STABLE"
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val contactPhone: String,
    val commodity: String,
    val quantity: Double,
    val unitName: String, // "100kg Bag", "Tons", "Crate", "Tubers"
    val pricePerUnit: Double, // in GHS
    val location: String, // e.g., Ejura, Sunyani, Techiman, Tamale
    val region: String, // e.g., Ashanti, Bono, Eastern, Northern
    val isOffer: Boolean, // true = selling (Farmer), false = buying (Buyer)
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
