package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgricRepository
import com.example.data.ListingEntity
import com.example.data.MarketPriceEntity
import com.example.data.LivestockRecordEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AdviceUiState {
    object Idle : AdviceUiState
    object Loading : AdviceUiState
    data class Success(val advice: String) : AdviceUiState
    data class Error(val message: String) : AdviceUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgricRepository(application)

    // Raw flows from Database
    val prices: StateFlow<List<MarketPriceEntity>> = repository.allPrices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val listings: StateFlow<List<ListingEntity>> = repository.allListings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val livestockRecords: StateFlow<List<LivestockRecordEntity>> = repository.allLivestockRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User inputs and UI state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedRegionFilter = MutableStateFlow("All")
    val selectedRegionFilter = _selectedRegionFilter.asStateFlow()

    private val _isOfferFilter = MutableStateFlow<Boolean?>(null) // null = All, true = Sellers/Farmers, false = Buyers
    val isOfferFilter = _isOfferFilter.asStateFlow()

    private val _adviceUiState = MutableStateFlow<AdviceUiState>(AdviceUiState.Idle)
    val adviceUiState = _adviceUiState.asStateFlow()

    private val _isOfflineSimulated = MutableStateFlow(false)
    val isOfflineSimulated = _isOfflineSimulated.asStateFlow()

    // Filtered listings derived from source listings, query and filters
    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        _searchQuery,
        _selectedRegionFilter,
        _isOfferFilter
    ) { rawListings, query, region, isOffer ->
        rawListings.filter { listing ->
            val matchQuery = listing.commodity.contains(query, ignoreCase = true) ||
                    listing.location.contains(query, ignoreCase = true) ||
                    listing.contactName.contains(query, ignoreCase = true)
            
            val matchRegion = region == "All" || listing.region.equals(region, ignoreCase = true)
            
            val matchOffer = isOffer == null || listing.isOffer == isOffer

            matchQuery && matchRegion && matchOffer
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateRegionFilter(region: String) {
        _selectedRegionFilter.value = region
    }

    fun updateOfferFilter(isOffer: Boolean?) {
        _isOfferFilter.value = isOffer
    }

    fun toggleOfflineSimulation() {
        _isOfflineSimulated.value = !_isOfflineSimulated.value
    }

    fun askAdvisor(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _adviceUiState.value = AdviceUiState.Loading
            if (_isOfflineSimulated.value) {
                // Simulate local high-fidelity database expert system query
                kotlinx.coroutines.delay(600)
                val localizedLower = prompt.lowercase()
                val offlineAdvice = when {
                    localizedLower.contains("vaccin") || localizedLower.contains("disease") || localizedLower.contains("health") -> {
                        """
                        • **OFFLINE EXPERT ADVISOR — VACCINATION SCHEDULES**
                        
                        *Source: Ghana Veterinary Services Directorate (VSD)*
                        
                        1. **Poultry (Broilers/Layers):**
                           - **Day 1:** Newcastle HB1 (eye-drop).
                           - **Day 7 & 14:** Gumboro (Ibd) vaccine.
                           - **Day 21:** Newcastle Lasota booster.
                           - **Week 6 & 12:** Fowl Pox vaccine.
                           - **Note:** Keep water sanitized, clean drinkers daily.
                           
                        2. **Goats & Sheep (Ruminants):**
                           - **PPR (Peste des Petits Ruminants):** Administer vaccine once annually (highly recommended before major harmattan dry session).
                           - **Deworming:** Every 3 months using Albendazole or Levamisole (especially at start of the rainy season in June).
                           
                        3. **Cattle:**
                           - **CBPP (Contagious Bovine Pleuropneumonia):** Annual compulsory vaccination.
                           - **Acaricide Spraying:** Weekly or bi-weekly to counter ticks carrying heartwater disease in the Coastal Savanna.
                        """.trimIndent()
                    }
                    localizedLower.contains("feed") || localizedLower.contains("ration") || localizedLower.contains("water") || localizedLower.contains("calculator") -> {
                        """
                        • **OFFLINE EXPERT ADVISOR — ANIMAL NUTRITION GUIDELINE**
                        
                        *Formulated for Ghanaian Agro-Ecological Zones (Dry Savanna, Transition, Forest)*
                        
                        1. **Goat & Sheep Feed Formulation:**
                           - Utilize standard **agro-industrial byproducts** like Cassava peels, Brewer's Spent Grain (BSG), and Palm Kernel Cake (PKC) mixed with dry grass (Panicum/Pennisetum).
                           - Maintain dry matter food intake at 3% to 4% of goat's live body weight.
                           
                        2. **Poultry Diet Requirements:**
                           - **Broiler Starter (Day 1-21):** Min 22% Crude Protein (CP) with rich maize/soya meal base.
                           - **Broiler Finisher (Day 22-Market):** Min 18% CP. Keep energy dense to build meat tissue.
                           - **Local formulation tip:** Incorporate dried cassava leaf meal and local oyster shells from Ada/Volta region for cheap calcium source.
                           
                        3. **Swine / Pig Feed Matrix:**
                           - Sows require 15% CP diet. Pigs are excellent converters of spent grains and organic kitchen peelings, but ensure they are thoroughly boiled to deter disease vectors.
                        """.trimIndent()
                    }
                    localizedLower.contains("breed") || localizedLower.contains("selection") -> {
                        """
                        • **OFFLINE EXPERT ADVISOR — BREED RECOMMENDATIONS FOR GHANA**
                        
                        1. **Goats:**
                           - **West African Dwarf (WAD):** Found mostly in Forest/South zones. High trypanotolerance (tsetse fly resistant). Compact size.
                           - **Sahel Goat:** Superb for Northern Ghana. Tall legislation structure, faster growth and larger carcass, but susceptible to humid forest pests.
                           
                        2. **Sheep:**
                           - **Djallonké Sheep:** Excellent local dwarf breed with high disease resistance and adaptiveness to rainy climates.
                           
                        3. **Poultry:**
                           - **Cobb 500 & Ross 308:** Choice broilers for commercial meat in Accra and Kumasi.
                           - **Noiler / Kuroiler:** Robust dual-purpose backyard breeds. Scavenge well, resilient in extreme heat, superior egg size.
                        """.trimIndent()
                    }
                    else -> {
                        """
                        • **OFFLINE EXPERT ADVISOR — GHANA AGRICULTURE INDEX**
                        
                        *Device fully offline. Loaded content from local SQLite (Room) Database Cache.*
                        
                        1. **Maize Cropping Cycle:**
                           - **Major Season:** Plant mid-March to April (South) or May/June (North). Ensure 5 cm sowing depth.
                           - **Post-harvest:** Dry immediately on tarpaulins to avoid Aspergillus/Aflatoxin contamination. Target moisture content is below 13%.
                           
                        2. **Pest Control:**
                           - **Fall Armyworm:** Spray Neem Kernel Extract (NKE) or organic soaps during cool evening hours when larvae feed actively.
                           
                        3. **Livestock Integration:**
                           - Practice integrated crop-livestock farming. Utilize goat and poultry droppings as premium nitrogen-rich organic manure for yam mounds and maize ridges.
                        """.trimIndent()
                    }
                }
                _adviceUiState.value = AdviceUiState.Success(offlineAdvice)
            } else {
                val response = repository.getAgriculturalAdvice(prompt)
                _adviceUiState.value = AdviceUiState.Success(response)
            }
        }
    }

    fun addListing(
        name: String,
        phone: String,
        commodity: String,
        quantity: Double,
        unit: String,
        price: Double,
        location: String,
        region: String,
        isOffer: Boolean,
        description: String
    ) {
        viewModelScope.launch {
            val newListing = ListingEntity(
                contactName = name,
                contactPhone = phone,
                commodity = commodity,
                quantity = quantity,
                unitName = unit,
                pricePerUnit = price,
                location = location,
                region = region,
                isOffer = isOffer,
                description = description
            )
            repository.insertListing(newListing)
        }
    }

    fun deleteListing(id: Int) {
        viewModelScope.launch {
            repository.deleteListingById(id)
        }
    }

    fun addLivestockRecord(
        breed: String,
        animalType: String,
        quantity: Int,
        location: String,
        nextVacc: String,
        vaccType: String,
        status: String,
        notes: String
    ) {
        viewModelScope.launch {
            val record = LivestockRecordEntity(
                breedName = breed,
                animalType = animalType,
                quantity = quantity,
                location = location,
                nextVaccinationDate = nextVacc,
                vaccineType = vaccType,
                status = status,
                localNotes = notes
            )
            repository.insertLivestockRecord(record)
        }
    }

    fun deleteLivestockRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteLivestockRecordById(id)
        }
    }
}
