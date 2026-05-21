package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "livestock_records")
data class LivestockRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val breedName: String, // e.g., "Sahel Goat", "Dwarf Sheep", "White Fulani Cattle", "Cobb 500 Broiler"
    val animalType: String, // "Goats", "Sheep", "Cattle", "Poultry", "Pigs"
    val quantity: Int,
    val location: String, // e.g. "Tamale Central", "Ejura Farm", "Koforidua Hub"
    val registrationDate: Long = System.currentTimeMillis(),
    val nextVaccinationDate: String, // "2026-06-15"
    val vaccineType: String, // "PPR Vaccine", "Newcastle I-2", "CBPP"
    val status: String, // "Healthy", "Under Treatment", "Quarantined"
    val localNotes: String = ""
)

@Dao
interface LivestockRecordDao {
    @Query("SELECT * FROM livestock_records ORDER BY registrationDate DESC")
    fun getAllLivestockRecords(): Flow<List<LivestockRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LivestockRecordEntity)

    @Query("DELETE FROM livestock_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<LivestockRecordEntity>)
}
