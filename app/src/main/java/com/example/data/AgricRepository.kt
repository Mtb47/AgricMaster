package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AgricRepository(context: Context) {
    private val db = AgricDatabase.getDatabase(context)
    private val priceDao = db.marketPriceDao()
    private val listingDao = db.marketplaceListingDao()
    private val livestockDao = db.livestockRecordDao()

    val allPrices: Flow<List<MarketPriceEntity>> = priceDao.getAllPrices()
    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val allLivestockRecords: Flow<List<LivestockRecordEntity>> = livestockDao.getAllLivestockRecords()

    suspend fun insertListing(listing: ListingEntity) = withContext(Dispatchers.IO) {
        listingDao.insertListing(listing)
    }

    suspend fun deleteListingById(id: Int) = withContext(Dispatchers.IO) {
        listingDao.deleteListingById(id)
    }

    suspend fun insertLivestockRecord(record: LivestockRecordEntity) = withContext(Dispatchers.IO) {
        livestockDao.insertRecord(record)
    }

    suspend fun deleteLivestockRecordById(id: Int) = withContext(Dispatchers.IO) {
        livestockDao.deleteRecordById(id)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentPrices = priceDao.getAllPrices().first()
        if (currentPrices.isEmpty()) {
            val initialPrices = listOf(
                MarketPriceEntity(commodity = "Yellow Maize", marketName = "Techiman Market", region = "Bono", pricePerUnit = 550.0, unitName = "100kg Bag", trend = "UP"),
                MarketPriceEntity(commodity = "White Yam (Pona)", marketName = "Makola Accra", region = "Greater Accra", pricePerUnit = 180.0, unitName = "5 Large Tubers", trend = "UP"),
                MarketPriceEntity(commodity = "Gari (Cassava)", marketName = "Kumasi Central", region = "Ashanti", pricePerUnit = 320.0, unitName = "50kg Bag", trend = "STABLE"),
                MarketPriceEntity(commodity = "Plantain (Apem)", marketName = "Ejura Market", region = "Ashanti", pricePerUnit = 95.0, unitName = "Medium Bunch", trend = "DOWN"),
                MarketPriceEntity(commodity = "Cocoa Beans", marketName = "COCOBOD Purchase Point", region = "Western North", pricePerUnit = 2080.0, unitName = "64kg Bag", trend = "STABLE"),
                MarketPriceEntity(commodity = "Local Rice", marketName = "Tamale Central", region = "Northern", pricePerUnit = 600.0, unitName = "50kg Bag", trend = "UP"),
                MarketPriceEntity(commodity = "Millet", marketName = "Bolgatanga Market", region = "Upper East", pricePerUnit = 480.0, unitName = "100kg Bag", trend = "STABLE")
            )
            priceDao.insertPrices(initialPrices)
        }

        // Also seed listing data if listings is empty
        val currentListings = listingDao.getAllListings().first()
        if (currentListings.isEmpty()) {
            val initialListings = listOf(
                ListingEntity(
                    contactName = "Kojo Boateng",
                    contactPhone = "+233244567890",
                    commodity = "White Yam (Pona)",
                    quantity = 250.0,
                    unitName = "Tubers",
                    pricePerUnit = 35.0,
                    location = "Ejura",
                    region = "Ashanti",
                    isOffer = true,
                    description = "Freshly harvested organic Yam. Pona variety, very sweet and high quality. Bulk purchase discounts available."
                ),
                ListingEntity(
                    contactName = "Ama Serwaa",
                    contactPhone = "+233201234567",
                    commodity = "Yellow Maize",
                    quantity = 15.0,
                    unitName = "100kg Bags",
                    pricePerUnit = 540.0,
                    location = "Techiman",
                    region = "Bono",
                    isOffer = true,
                    description = "Well dried Maize under standard conditions. Moisture content below 12%. Ready for delivery."
                ),
                ListingEntity(
                    contactName = "Yussif Ibrahim",
                    contactPhone = "+233267891234",
                    commodity = "Local Rice",
                    quantity = 50.0,
                    unitName = "50kg Bags",
                    pricePerUnit = 620.0,
                    location = "Tamale",
                    region = "Northern",
                    isOffer = false,
                    description = "Looking for premium quality locally processed rice to wholesale in Accra. Must be well destoned."
                )
            )
            for (listing in initialListings) {
                listingDao.insertListing(listing)
            }
        }

        // Seed initial livestock records
        val currentLivestock = livestockDao.getAllLivestockRecords().first()
        if (currentLivestock.isEmpty()) {
            val initialLivestock = listOf(
                LivestockRecordEntity(
                    breedName = "Sahel Goat",
                    animalType = "Goats",
                    quantity = 45,
                    location = "Tamale Ranch",
                    nextVaccinationDate = "2026-06-20",
                    vaccineType = "PPR Vaccine",
                    status = "Healthy",
                    localNotes = "Tolerant to dry climates. Fed on forage and supplementary concentrate."
                ),
                LivestockRecordEntity(
                    breedName = "Djallonké Sheep",
                    animalType = "Sheep",
                    quantity = 30,
                    location = "Techiman Hub",
                    nextVaccinationDate = "2026-07-05",
                    vaccineType = "PPR Vaccine",
                    status = "Healthy",
                    localNotes = "High dwarf-breed disease resistance. Currently grazing in open pastures."
                ),
                LivestockRecordEntity(
                    breedName = "Cobb 500 poultry",
                    animalType = "Poultry",
                    quantity = 1200,
                    location = "Ejura Farm",
                    nextVaccinationDate = "2026-05-28",
                    vaccineType = "Newcastle I-2",
                    status = "Healthy",
                    localNotes = "Day 24 broilers. Active feeding schedule. Stocking rate is within standard norms."
                ),
                LivestockRecordEntity(
                    breedName = "White Fulani Cattle",
                    animalType = "Cattle",
                    quantity = 14,
                    location = "Sunyani Outpost",
                    nextVaccinationDate = "2026-08-12",
                    vaccineType = "CBPP Vaccine",
                    status = "Under Treatment",
                    localNotes = "2 heifers recovering from minor foot-rot. Vet inspection scheduled."
                ),
                LivestockRecordEntity(
                    breedName = "Large White Pig",
                    animalType = "Pigs",
                    quantity = 28,
                    location = "Koforidua Sty",
                    nextVaccinationDate = "2026-06-12",
                    vaccineType = "Erysipelas Vaccine",
                    status = "Healthy",
                    localNotes = "Breeding stock. Fed on wheat bran and palm-kernel cake mash."
                )
            )
            livestockDao.insertRecords(initialLivestock)
        }
    }

    suspend fun getAgriculturalAdvice(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API key not configured in the Secrets panel of AI Studio.\n\n" +
                    "To talk to the live intelligent Agric Master advisor, please configure your GEMINI_API_KEY secret. " +
                    "For now, here is some default agricultural guidance for Ghana:\n\n" +
                    "• **Maize Planting (Major Season):** Best to plant as soon as the rains stabilize (typically March/April in Southern/Middle Ghana, and May/June in Northern Ghana).\n" +
                    "• **Pest Advisory:** Keep a keen eye out for Fall Armyworm. Early detection and spraying with neem-based botanicals or organic biopesticides works wonders.\n" +
                    "• **Marketing Tip:** Connect with buyers during harvests or store your grains in certified warehouses to sell during lean seasons when prices appreciate by up to 40%."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Agric Master AI Advisor, an expert agronomist specialized in Ghanaian farming systems, crop cultivation cycles, pricing strategies, market insights, and pest management. Give practical, high-value advice using local terms (e.g. Pona, Gari, COCOBOD, Techiman, ejura, and key regions of Ghana like Bono, Ashanti, Northern, Greater Accra). Keep responses concise, beautifully structured in markdown with bullet points, and extremely helpful. Focus heavily on actual Ghanaian farming contexts, major rainy season (March to July) vs minor rainy season (September to November), and the harmattan season."))
            ),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to process advice at this time. Please try again."
        } catch (e: Exception) {
            "Network error when connecting to Gemini AI: ${e.localizedMessage ?: "Unknown Error"}. Please check your internet connection."
        }
    }
}
