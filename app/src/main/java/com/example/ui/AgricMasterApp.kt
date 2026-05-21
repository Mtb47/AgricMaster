package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ListingEntity
import com.example.data.MarketPriceEntity
import com.example.ui.theme.*

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSizeClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgricMasterApp(viewModel: MainViewModel) {
    val windowSizeClass = rememberWindowSizeClass()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Market Prices, 1 = Marketplace, 2 = AI Advisor, 3 = Livestock, 4 = Game Sim

    // Observe DB States
    val prices by viewModel.prices.collectAsStateWithLifecycle()
    val listings by viewModel.filteredListings.collectAsStateWithLifecycle()
    val livestockRecords by viewModel.livestockRecords.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val regionFilter by viewModel.selectedRegionFilter.collectAsStateWithLifecycle()
    val offerFilter by viewModel.isOfferFilter.collectAsStateWithLifecycle()
    val adviceUiState by viewModel.adviceUiState.collectAsStateWithLifecycle()
    val isOfflineSimulated by viewModel.isOfflineSimulated.collectAsStateWithLifecycle()

    var showAddListingDialog by remember { mutableStateOf(false) }
    var showAddLivestockDialog by remember { mutableStateOf(false) }
    var selectedListingForDetail by remember { mutableStateOf<ListingEntity?>(null) }
    var selectedPriceForAiAnalysis by remember { mutableStateOf<MarketPriceEntity?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column {
                    // Main Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter A avatar in small Rounded-xl container
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ForestGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Agric Master",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CharcoalText
                            )
                            Text(
                                text = "GHANA FARMER PORTAL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MutedClay,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        // Offline Indicator Switch/Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isOfflineSimulated) Color(0xFFFCE8E6) else Color(0xFFE6F4EA)
                                )
                                .clickable { viewModel.toggleOfflineSimulation() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isOfflineSimulated) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                                    contentDescription = "Simulated Connection",
                                    tint = if (isOfflineSimulated) Color(0xFFC5221F) else Color(0xFF137333),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOfflineSimulated) "OFFLINE" else "ONLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineSimulated) Color(0xFFC5221F) else Color(0xFF137333)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Active insight trigger & Profile avatar
                        IconButton(
                            onClick = {
                                val prompt = if (isOfflineSimulated) "Suggest a crop rotational plan for Ejura" else "Analyze the Ghana agricultural crop schedule for this month."
                                viewModel.askAdvisor(prompt)
                                selectedTab = 2
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Daily Insight",
                                tint = HarvestGoldSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightLeafGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "KO",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Market Ticker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LiveTickerBg)
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ForestGreenPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Maize: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                                Text("GHS 8.50 ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiveTickerText)
                                Text("↑2%", fontSize = 9.sp, color = LiveTickerText)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Cocoa: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                                Text("GHS 14.20 ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiveTickerText)
                                Text("↑0.5%", fontSize = 9.sp, color = LiveTickerText)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Yam: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                                Text("GHS 22.00 ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LiveTickerText)
                                Text("↓1.2%", fontSize = 9.sp, color = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (windowSizeClass == WindowSizeClass.COMPACT) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Market Prices") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.TrendingUp else Icons.Outlined.TrendingUp,
                                contentDescription = "Prices Tab"
                            )
                        },
                        modifier = Modifier.testTag("nav_prices_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Marketplace") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Marketplace Tab"
                            )
                        },
                        modifier = Modifier.testTag("nav_marketplace_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Livestock") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Default.Pets else Icons.Outlined.Pets,
                                contentDescription = "Livestock Tab"
                            )
                        },
                        modifier = Modifier.testTag("nav_livestock_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        label = { Text("Arcade") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 4) Icons.Default.SportsEsports else Icons.Outlined.SportsEsports,
                                contentDescription = "Arcade Sim Tab"
                            )
                        },
                        modifier = Modifier.testTag("nav_arcade_tab")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("AI Advisor") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.Assistant else Icons.Outlined.Assistant,
                                contentDescription = "Advisor Tab"
                            )
                        },
                        modifier = Modifier.testTag("nav_advisor_tab")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddListingDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_listing")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Post Listing")
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddLivestockDialog = true },
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_livestock")
                ) {
                    Icon(imageVector = Icons.Default.Pets, contentDescription = "Log Herd")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Left Navigation Rail for foldables / tablets
            if (windowSizeClass != WindowSizeClass.COMPACT) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                ) {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Prices") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.TrendingUp else Icons.Outlined.TrendingUp,
                                contentDescription = "Nav Rail Prices"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Marketplace") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Nav Rail Marketplace"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Livestock") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Default.Pets else Icons.Outlined.Pets,
                                contentDescription = "Nav Rail Livestock"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        label = { Text("Arcade") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 4) Icons.Default.SportsEsports else Icons.Outlined.SportsEsports,
                                contentDescription = "Nav Rail Arcade"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("AI Advisor") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.Assistant else Icons.Outlined.Assistant,
                                contentDescription = "Nav Rail Advisor"
                            )
                        }
                    )
                }
            }
 
            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Crossfade(targetState = selectedTab, label = "TabNavigation") { tab ->
                    when (tab) {
                        0 -> MarketPricesTabContent(
                            prices = prices,
                            windowSizeClass = windowSizeClass,
                            selectedPriceForAnalysis = selectedPriceForAiAnalysis,
                            onPriceSelectedForAnalysis = { selectedPriceForAiAnalysis = it },
                            onAskGemini = { commodityName ->
                                viewModel.askAdvisor("Analyze pricing trends, consumer demand, and harvesting cycles in Ghana for $commodityName.")
                                selectedTab = 2
                            },
                            onViewAllBuyers = { selectedTab = 1 }
                        )
                        1 -> MarketplaceTabContent(
                            listings = listings,
                            searchQuery = searchQuery,
                            regionFilter = regionFilter,
                            offerFilter = offerFilter,
                            onSearchQueryChanged = viewModel::updateSearchQuery,
                            onRegionFilterChanged = viewModel::updateRegionFilter,
                            onOfferFilterChanged = viewModel::updateOfferFilter,
                            windowSizeClass = windowSizeClass,
                            selectedListing = selectedListingForDetail,
                            onListingSelected = { selectedListingForDetail = it },
                            onDeleteListing = { listingId ->
                                viewModel.deleteListing(listingId)
                                if (selectedListingForDetail?.id == listingId) {
                                    selectedListingForDetail = null
                                }
                            }
                        )
                        2 -> AssistantTabContent(
                            adviceUiState = adviceUiState,
                            onAskAdvisor = viewModel::askAdvisor
                        )
                        3 -> LivestockTabContent(
                            records = livestockRecords,
                            isOffline = isOfflineSimulated,
                            onDeleteRecord = viewModel::deleteLivestockRecord,
                            onViewGuidelines = {
                                viewModel.askAdvisor(it)
                                selectedTab = 2
                            }
                        )
                        4 -> AgricArcadeTabContent(
                            isOffline = isOfflineSimulated,
                            onNavigateToAdvisor = { prompt ->
                                viewModel.askAdvisor(prompt)
                                selectedTab = 2
                            }
                        )
                    }
                }
            }
        }
    }

        // Add Listing Dialog
        if (showAddListingDialog) {
            AddListingDialog(
                onDismiss = { showAddListingDialog = false },
                onConfirm = { name, phone, crop, quantity, unit, price, loc, reg, typeSeller, desc ->
                    viewModel.addListing(name, phone, crop, quantity, unit, price, loc, reg, typeSeller, desc)
                    showAddListingDialog = false
                }
            )
        }

        // Add Livestock Dialog
        if (showAddLivestockDialog) {
            AddLivestockDialog(
                onDismiss = { showAddLivestockDialog = false },
                onConfirm = { breed, animalType, qty, loc, nextVacc, vaccType, status, notes ->
                    viewModel.addLivestockRecord(breed, animalType, qty, loc, nextVacc, vaccType, status, notes)
                    showAddLivestockDialog = false
                }
            )
        }
    }

// ---------------------------------------------------------------------------------
// HIGH DENSITY THEME SPECIFIC WIDGETS
// ---------------------------------------------------------------------------------
@Composable
fun MarketIndexCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, HighDensityBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Market Index",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = "Price trends across regions",
                        fontSize = 11.sp,
                        color = MutedClay
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+4.2% Week",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful High-density layout bars chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val heights = listOf(0.40f, 0.60f, 0.85f, 0.55f, 0.70f, 0.95f, 0.50f, 0.65f)
                val colors = listOf(
                    LightGreenContainer, LightGreenContainer, ForestGreenPrimary,
                    LightGreenContainer, LightGreenContainer, ForestGreenPrimary,
                    LightGreenContainer, LightGreenContainer
                )

                heights.forEachIndexed { idx, hWeight ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(hWeight)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(colors[idx])
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TAMALE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                Text("KUMASI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                Text("ACCRA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                Text("KOFORIDUA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedClay)
            }
        }
    }
}

@Composable
fun WeatherAndBuyersPanel(onViewAllBuyers: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Weather / Soil Mini Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = ForestGreenPrimary),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Cloudy update",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "28°C",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "Weather Update",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Scattered Showers",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Buyer Connections Card
        Card(
            modifier = Modifier
                .weight(1f)
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7E9E1)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Buyer Requests",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedClay,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Connection 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EP needs 500kg Ginger",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = CharcoalText
                        )
                    }

                    // Connection 2
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("KM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "K. Mensah needs Maize",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = CharcoalText
                        )
                    }
                }

                Button(
                    onClick = onViewAllBuyers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("VIEW ALL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 1: MARKET PRICES
// ---------------------------------------------------------------------------------
@Composable
fun MarketPricesTabContent(
    prices: List<MarketPriceEntity>,
    windowSizeClass: WindowSizeClass,
    selectedPriceForAnalysis: MarketPriceEntity?,
    onPriceSelectedForAnalysis: (MarketPriceEntity?) -> Unit,
    onAskGemini: (String) -> Unit,
    onViewAllBuyers: () -> Unit
) {
    if (prices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (windowSizeClass == WindowSizeClass.EXPANDED) {
            // Double Pane Layout for Large Screens
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1.1f)) {
                    MarketPricesList(
                        prices = prices,
                        selectedItem = selectedPriceForAnalysis,
                        onItemClick = onPriceSelectedForAnalysis,
                        onViewAllBuyers = onViewAllBuyers
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    if (selectedPriceForAnalysis != null) {
                        MarketPriceDetailPane(
                            priceItem = selectedPriceForAnalysis,
                            onAskGemini = onAskGemini,
                            onClose = { onPriceSelectedForAnalysis(null) }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Select to view",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Select a Commodity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Choose any Crop from the database to view regional price details, price metrics, and request Gemini AI analysis.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Single Pane scroll list with expandable details
            var activeExpandedId by remember { mutableStateOf<Int?>(null) }
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 1. High Density Dashboard Cards
                    item {
                        MarketIndexCard()
                    }
                    item {
                        WeatherAndBuyersPanel(onViewAllBuyers = onViewAllBuyers)
                    }

                    // 2. Section Header Title
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                "Ghana Regional Commodity Market Feed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Cached offline. Source: Ministry of Food & Agriculture (MoFA) Municipal Desks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Main Commodity Prices list
                    items(prices) { item ->
                        MarketPriceItemBlock(
                            item = item,
                            isExpanded = activeExpandedId == item.id,
                            onClick = {
                                activeExpandedId = if (activeExpandedId == item.id) null else item.id
                            },
                            onAskGemini = onAskGemini
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketPricesList(
    prices: List<MarketPriceEntity>,
    selectedItem: MarketPriceEntity?,
    onItemClick: (MarketPriceEntity) -> Unit,
    onViewAllBuyers: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High Density cards for tablet screens
            item {
                MarketIndexCard()
            }
            item {
                WeatherAndBuyersPanel(onViewAllBuyers = onViewAllBuyers)
            }

            item {
                Text(
                    "Ghana Regional Market Feed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(prices) { item ->
                val isSelected = selectedItem?.id == item.id
                Card(
                    onClick = { onItemClick(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("price_item_${item.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = CardBorder(isSelected)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    item.commodity,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PriceTrendBadge(trend = item.trend)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "location",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "${item.marketName} (${item.region} Region)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "GHS ${String.format("%.2f", item.pricePerUnit)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "per ${item.unitName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardBorder(selected: Boolean): BorderStroke? {
    return if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    }
}

@Composable
fun PriceTrendBadge(trend: String) {
    val bgColor = when (trend) {
        "UP" -> Color(0xFFE8F5E9)
        "DOWN" -> Color(0xFFFFEBEE)
        else -> Color(0xFFECEFF1)
    }
    val contentColor = when (trend) {
        "UP" -> Color(0xFF2E7D32)
        "DOWN" -> Color(0xFFC62828)
        else -> Color(0xFF455A64)
    }
    val label = when (trend) {
        "UP" -> "▲ Upward"
        "DOWN" -> "▼ Downward"
        else -> "● Stable"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun MarketPriceItemBlock(
    item: MarketPriceEntity,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onAskGemini: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("price_block_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.commodity,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        PriceTrendBadge(trend = item.trend)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "loc",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "${item.marketName} • ${item.region} Region",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "GHS ${String.format("%.2f", item.pricePerUnit)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "per ${item.unitName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Agricultural Insights",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Farming conditions, regional soil cycles, and minor rainy seasons are influencing price stability. For detail analysis on this commodity, request our smart advisor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAskGemini(item.commodity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_btn_${item.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask Agric Master AI about ${item.commodity}")
                    }
                }
            }
        }
    }
}

@Composable
fun MarketPriceDetailPane(
    priceItem: MarketPriceEntity,
    onAskGemini: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Price Detail Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close detailed pane")
            }
        }
        Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            priceItem.commodity,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        PriceTrendBadge(trend = priceItem.trend)

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Price Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "GHS ${String.format("%.2f", priceItem.pricePerUnit)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Measurement Unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    priceItem.unitName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Market Spot Location", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            priceItem.marketName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Region: ${priceItem.region} Ghana",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Smart Advisor Analysis",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Receive precise price estimation, local demand updates, and agronomy patterns customized for ${priceItem.commodity} using Gemini AI.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onAskGemini(priceItem.commodity) },
                    modifier = Modifier.fillMaxWidth().testTag("large_pane_analyze_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Assistant, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze crop in advisor")
                }
            }
        }
    }
}


// ---------------------------------------------------------------------------------
// TAB 2: CROP MARKETPLACE
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceTabContent(
    listings: List<ListingEntity>,
    searchQuery: String,
    regionFilter: String,
    offerFilter: Boolean?,
    onSearchQueryChanged: (String) -> Unit,
    onRegionFilterChanged: (String) -> Unit,
    onOfferFilterChanged: (Boolean?) -> Unit,
    windowSizeClass: WindowSizeClass,
    selectedListing: ListingEntity?,
    onListingSelected: (ListingEntity?) -> Unit,
    onDeleteListing: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MarketplaceFilterHeader(
            searchQuery = searchQuery,
            selectedRegion = regionFilter,
            selectedOffer = offerFilter,
            onSearchChanged = onSearchQueryChanged,
            onRegionChanged = onRegionFilterChanged,
            onOfferChanged = onOfferFilterChanged
        )

        if (listings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Browse empty",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Marketplace Listings Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Try relaxing your search/region filters, or create the first listing by clicking the '+' button below!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            if (windowSizeClass == WindowSizeClass.EXPANDED) {
                // Large screen master-detail layouts
                Row(modifier = Modifier.weight(1.1f)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MarketplaceListPane(
                            listings = listings,
                            selectedListing = selectedListing,
                            onListingClick = onListingSelected
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        if (selectedListing != null) {
                            MarketplaceDetailPaneContent(
                                listing = selectedListing,
                                onDelete = { onDeleteListing(selectedListing.id) },
                                onClose = { onListingSelected(null) }
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = "No select",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Listing Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Choose any offer or request listing to preview crop characteristics, farm details, direct dealer contacts, and region insights.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                // Mobile list view
                LazyVerticalGrid(
                    columns = if (windowSizeClass == WindowSizeClass.MEDIUM) GridCells.Fixed(2) else GridCells.Fixed(1),
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listings) { listing ->
                        MarketplaceMobileCard(
                            listing = listing,
                            onClick = { onListingSelected(listing) }
                        )
                    }
                }
            }
        }
    }

    if (selectedListing != null && windowSizeClass != WindowSizeClass.EXPANDED) {
        // Mobile details dialogue sheet fallback
        Dialog(onDismissRequest = { onListingSelected(null) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                MarketplaceDetailPaneContent(
                    listing = selectedListing,
                    onDelete = {
                        onDeleteListing(selectedListing.id)
                        onListingSelected(null)
                    },
                    onClose = { onListingSelected(null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceFilterHeader(
    searchQuery: String,
    selectedRegion: String,
    selectedOffer: Boolean?,
    onSearchChanged: (String) -> Unit,
    onRegionChanged: (String) -> Unit,
    onOfferChanged: (Boolean?) -> Unit
) {
    val regions = listOf("All", "Ashanti", "Bono", "Eastern", "Northern", "Greater Accra", "Western")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("Search crop, region, dealer...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("marketplace_search_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Role Filter Row (Sellers/Buyers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedOffer == null,
                onClick = { onOfferChanged(null) },
                label = { Text("All Listings") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("filter_offer_all")
            )
            FilterChip(
                selected = selectedOffer == true,
                onClick = { onOfferChanged(true) },
                label = { Text("Sellers (Farmers)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("filter_offer_sellers")
            )
            FilterChip(
                selected = selectedOffer == false,
                onClick = { onOfferChanged(false) },
                label = { Text("Buyers (Requests)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("filter_offer_buyers")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Region Scroll Horizontal Row
        Text("Region Filter:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            regions.forEach { regionName ->
                val isSelected = selectedRegion == regionName
                SuggestionChip(
                    onClick = { onRegionChanged(regionName) },
                    label = { Text(regionName, fontSize = 12.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("chip_region_$regionName")
                )
            }
        }
    }
}

@Composable
fun MarketplaceListPane(
    listings: List<ListingEntity>,
    selectedListing: ListingEntity?,
    onListingClick: (ListingEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(listings) { item ->
            val isSelected = selectedListing?.id == item.id
            Card(
                onClick = { onListingClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("marketplace_item_card_${item.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                border = CardBorder(isSelected)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ListingRoleBadge(isOffer = item.isOffer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                item.commodity,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Dealer: ${item.contactName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${item.location} (${item.region} Region)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "GHS ${String.format("%.2f", item.pricePerUnit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${item.quantity} ${item.unitName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceMobileCard(
    listing: ListingEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("marketplace_mobile_item_${listing.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ListingRoleBadge(isOffer = listing.isOffer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        listing.commodity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "GHS ${String.format("%.2f", listing.pricePerUnit)}",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Dealer Name", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(listing.contactName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock Volume", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${listing.quantity} ${listing.unitName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${listing.location}, ${listing.region} Region",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ListingRoleBadge(isOffer: Boolean) {
    val containerBg = if (isOffer) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
    val textStyleColor = if (isOffer) Color(0xFF1B5E20) else Color(0xFF0D47A1)
    val labelText = if (isOffer) "SELL" else "BUY"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = labelText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = textStyleColor
        )
    }
}

@Composable
fun MarketplaceDetailPaneContent(
    listing: ListingEntity,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ListingRoleBadge(isOffer = listing.isOffer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (listing.isOffer) "Farmer Sale Listing" else "Buyer Demand Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Detail modal")
            }
        }
        Divider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            listing.commodity,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Rate Offered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("GHS ${String.format("%.2f", listing.pricePerUnit)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("per ${listing.unitName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Volume Available", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${listing.quantity} Units", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Type: ${listing.unitName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Description & Custom Details", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            Text(
                text = listing.description.ifBlank { "No additional agronomy or transport description loaded for this listing." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Farming Hub Location", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "${listing.location}, ${listing.region} Region, Ghana",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(16.dp))

        // Contact info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.2f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(listing.contactName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Active Ag-Dealer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.contactPhone}"))
                            try {
                                context.startActivity(callIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dialer not supported. Phone: ${listing.contactPhone}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("call_dealer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Direct")
                    }

                    OutlinedButton(
                        onClick = {
                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${listing.contactPhone}"))
                            smsIntent.putExtra("sms_body", "Hello ${listing.contactName}, is your ${listing.commodity} listed on Agric Master still available?")
                            try {
                                context.startActivity(smsIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "MESSAGES not supported", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("message_dealer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "sms", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SMS Chat")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Option to delete if the user feels it's their listing (demonstration capability)
        TextButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().testTag("delete_listing_btn"),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Listing (Demo Option)")
        }
    }
}


// ---------------------------------------------------------------------------------
// TAB 3: SMART AGRIC AI ADVISOR
// ---------------------------------------------------------------------------------
@Composable
fun AssistantTabContent(
    adviceUiState: AdviceUiState,
    onAskAdvisor: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val preloadedQueries = listOf(
        " cocoa weather schedule",
        "How to combat Cocoa Swollen Shoot Disease?",
        "Best fertilizer for Techiman Maize planting",
        "How to store Gari long-term safely?",
        "Suggest a crop rotational plan for Ejura"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Ghana Smart Agric Advisor (Gemini)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
            Text(
                "Pose questions about soil conditions, planting timelines, crop pests (like Fall Armyworm), or COCOBOD regulations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conversations display area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(12.dp)
            ) {
                when (adviceUiState) {
                    is AdviceUiState.Idle -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assistant,
                                contentDescription = "assistant",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Your Agronomy Assistant is online",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ask or tap a suggested Ghanaian agriculture query below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Suggested Farm Topics:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            preloadedQueries.forEach { suggestion ->
                                Card(
                                    onClick = { onAskAdvisor(suggestion) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(suggestion, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                    is AdviceUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analyzing Ghana agricultural schedules...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is AdviceUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(0.1f)).padding(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Assistant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Expert Advice Generated", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = adviceUiState.advice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                    is AdviceUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(adviceUiState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input send form bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask anything about Ghana farming...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("advisor_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            onAskAdvisor(textInput)
                            textInput = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onAskAdvisor(textInput)
                        textInput = ""
                        focusManager.clearFocus()
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("advisor_send_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send advice prompt"
                )
            }
        }
    }
}


// ---------------------------------------------------------------------------------
// ADD LISTING DIALOG COMPOSABLE
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddListingDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        crop: String,
        quantity: Double,
        unit: String,
        price: Double,
        location: String,
        region: String,
        isOffer: Boolean,
        description: String
    ) -> Unit
) {
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var cropName by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var unitName by remember { mutableStateOf("100kg Bag") }
    var priceStr by remember { mutableStateOf("") }
    var farmLocation by remember { mutableStateOf("") }
    var regionSelected by remember { mutableStateOf("Ashanti") }
    var listingTypeSeller by remember { mutableStateOf(true) } // true=Seller, false=Buyer
    var customDescription by remember { mutableStateOf("") }

    val regions = listOf("Ashanti", "Bono", "Eastern", "Northern", "Greater Accra", "Western", "Volta")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_listing_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    "Post Agriculture Listing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Add detailed agronomy stock info to broadcast live to target dealers in Ghana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Listing Type Switch (Farmers Selling crop vs. Buyers requesting)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { listingTypeSeller = true },
                        modifier = Modifier.weight(1f).testTag("select_seller_type"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (listingTypeSeller) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            "Sell Offer (Farmer)",
                            color = if (listingTypeSeller) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { listingTypeSeller = false },
                        modifier = Modifier.weight(1f).testTag("select_buyer_type"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!listingTypeSeller) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            "Buy Request (Dealer)",
                            color = if (!listingTypeSeller) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Crop Commodity Name
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Commodity (e.g. Maize, Yam, Cocoa)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_crop_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Price estimation
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (GHS)") },
                        modifier = Modifier.weight(1f).testTag("add_price"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    // Volume quantity
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f).testTag("add_quantity"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unit Type Option Input
                OutlinedTextField(
                    value = unitName,
                    onValueChange = { unitName = it },
                    label = { Text("Unit (e.g. 100kg Bag, Tubers, Tons, Box)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_unit"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Contact Section
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Dealer Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dealer_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Dealer Phone Number (e.g., +233...)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_dealer_phone"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = farmLocation,
                        onValueChange = { farmLocation = it },
                        label = { Text("Town/Village") },
                        modifier = Modifier.weight(1f).testTag("add_location"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Region filtering tags selector
                Text("Region in Ghana:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    regions.forEach { reg ->
                        val selected = regionSelected == reg
                        InputChip(
                            selected = selected,
                            onClick = { regionSelected = reg },
                            label = { Text(reg, fontSize = 11.sp) },
                            modifier = Modifier.testTag("add_region_chip_$reg")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = customDescription,
                    onValueChange = { customDescription = it },
                    label = { Text("Quality description, logistics...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("add_description"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions Confirm/Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("add_post_cancel")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val quant = quantityStr.toDoubleOrNull() ?: 1.0
                            val prc = priceStr.toDoubleOrNull() ?: 0.0
                            if (cropName.isNotBlank() && contactName.isNotBlank() && contactPhone.isNotBlank() && farmLocation.isNotBlank()) {
                                onConfirm(
                                    contactName,
                                    contactPhone,
                                    cropName,
                                    quant,
                                    unitName,
                                    prc,
                                    farmLocation,
                                    regionSelected,
                                    listingTypeSeller,
                                    customDescription
                                )
                            }
                        },
                        modifier = Modifier.testTag("add_post_confirm"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Post Broadcast")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 4: LIVESTOCK REARING & OFF-LINE ACTION CENTRE
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LivestockTabContent(
    records: List<com.example.data.LivestockRecordEntity>,
    isOffline: Boolean,
    onDeleteRecord: (Int) -> Unit,
    onViewGuidelines: (String) -> Unit
) {
    var calcAnimalType by remember { mutableStateOf("Poultry") }
    var calcHeadsText by remember { mutableStateOf("100") }

    val feedCalculation = remember(calcAnimalType, calcHeadsText) {
        val heads = calcHeadsText.toIntOrNull() ?: 0
        val feedPerHead = when (calcAnimalType) {
            "Poultry" -> 0.12 // 120g
            "Goats" -> 1.5 // 1.5kg
            "Sheep" -> 1.8 // 1.8kg
            "Pigs" -> 2.5 // 2.5kg
            "Cattle" -> 12.0 // 12kg
            else -> 1.0
        }
        val waterPerHead = when (calcAnimalType) {
            "Poultry" -> 0.25 // 250ml
            "Goats" -> 4.5 // 4.5L
            "Sheep" -> 5.0 // 5.0L
            "Pigs" -> 8.0 // 8L
            "Cattle" -> 40.0 // 40L
            else -> 2.0
        }
        Pair(heads * feedPerHead, heads * waterPerHead)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("livestock_content_scroll")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Banner & Header
        item {
            Column {
                Text(
                    text = "Livestock Husbandry Portal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "Track vaccination schedules, compute accurate feed intakes, and access veterinary emergency guidelines offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Offline Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOffline) Color(0xFFFEF7E0) else Color(0xFFE6F4EA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Wifi,
                        contentDescription = "Status Details",
                        tint = if (isOffline) Color(0xFFB06000) else Color(0xFF137333),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOffline) "Offline Local Cache Secured" else "Cloud Sync Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isOffline) Color(0xFFB06000) else Color(0xFF137333)
                        )
                        Text(
                            text = if (isOffline) "Device offline simulation enabled. Writing directly to local Room SQLite safely."
                            else "Connected live. Broadcasting records directly to master database stream.",
                            fontSize = 10.sp,
                            color = if (isOffline) Color(0xFF5F6368) else Color(0xFF1F2020)
                        )
                    }
                }
            }
        }

        // Inventory Metrics Summary Panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Population Metric Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Animal Count", fontSize = 10.sp, color = MutedClay, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${records.sumOf { it.quantity }} Head",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }
                }

                // Active Groups Metric Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Herds", fontSize = 10.sp, color = MutedClay, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${records.size} Herds",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    }
                }

                // Treat Alert Metric Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Attention Reg.", fontSize = 10.sp, color = MutedClay, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${records.count { it.status != "Healthy" }} Alerts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (records.any { it.status != "Healthy" }) Color(0xFFC5221F) else ForestGreenPrimary
                        )
                    }
                }
            }
        }

        // Outbreaks Alert & Practice Guidelines
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VETERINARY OUTBREAKS ACTION CENTRE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedClay,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onViewGuidelines("Show vaccination schedules for goats, sheep, poultry and cattle in Ghana.") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("VACCINES", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { onViewGuidelines("Formulate high-yield organic animal feeds and water ratios using Ghana cassava peels and grains.") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("NUTRITION", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = { onViewGuidelines("Suggest best cattle, goat and poultry breeds for heat resistance and high yield in Northern vs Southern Ghana.") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Grass, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("BREEDS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Dynamic Nutrition Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FEED & WATER CONV. CALCULATOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MutedClay
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val animals = listOf("Poultry", "Goats", "Sheep", "Pigs", "Cattle")
                        FlowRow(
                            modifier = Modifier.weight(1.3f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            animals.forEach { animal ->
                                val active = calcAnimalType == animal
                                FilterChip(
                                    selected = active,
                                    onClick = { calcAnimalType = animal },
                                    label = { Text(animal, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("calc_chip_$animal")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = calcHeadsText,
                            onValueChange = { calcHeadsText = it },
                            label = { Text("Heads") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.7f)
                                .testTag("calc_heads_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estimated Daily Feed:", fontSize = 10.sp, color = MutedClay)
                            Text(
                                text = "${String.format("%.2f", feedCalculation.first)} kg/day",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenPrimary
                            )
                        }
                        Column {
                            Text("Estimated Daily Water:", fontSize = 10.sp, color = MutedClay)
                            Text(
                                text = "${String.format("%.2f", feedCalculation.second)} L/day",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A73E8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "*General estimate based MoFA standard guides.",
                        fontSize = 9.sp,
                        color = MutedClay
                    )
                }
            }
        }

        // Header and List of Herds
        item {
            Text(
                text = "Registered Herds Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local herds logged. Click lower '+' to spawn new animal records.", color = MutedClay, fontSize = 11.sp)
                }
            }
        } else {
            items(records) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("herd_item_${record.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ForestGreenPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = record.breedName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = "${record.animalType} | ${record.location}",
                                        fontSize = 11.sp,
                                        color = MutedClay
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteRecord(record.id) },
                                modifier = Modifier.testTag("delete_herd_${record.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete record",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = HighDensityBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("STOCK VOLUME", fontSize = 9.sp, color = MutedClay, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${record.quantity} heads",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DISEASE DEFENCE STATUS", fontSize = 9.sp, color = MutedClay, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (record.status) {
                                                "Healthy" -> Color(0xFFE6F4EA)
                                                "Under Treatment" -> Color(0xFFFEF7E0)
                                                else -> Color(0xFFFCE8E6)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = record.status.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (record.status) {
                                            "Healthy" -> Color(0xFF137333)
                                            "Under Treatment" -> Color(0xFFB06000)
                                            else -> Color(0xFFC5221F)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8F9FA))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = HarvestGoldSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Vaccination Alert: ${record.nextVaccinationDate}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                }
                                Text(
                                    text = "Requires: ${record.vaccineType}",
                                    fontSize = 10.sp,
                                    color = MutedClay,
                                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                                )
                                if (record.localNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Notes: ${record.localNotes}",
                                        fontSize = 11.sp,
                                        color = CharcoalText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Add Livestock Dialog
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddLivestockDialog(
    onDismiss: () -> Unit,
    onConfirm: (breed: String, animalType: String, qty: Int, loc: String, nextVacc: String, vaccType: String, status: String, notes: String) -> Unit
) {
    var breedName by remember { mutableStateOf("") }
    var animalType by remember { mutableStateOf("Poultry") }
    var quantityStr by remember { mutableStateOf("100") }
    var locationName by remember { mutableStateOf("Ejura Farm") }
    var nextVaccDate by remember { mutableStateOf("2026-06-25") }
    var vaccineType by remember { mutableStateOf("Newcastle I-2") }
    var healthStatus by remember { mutableStateOf("Healthy") }
    var customNotes by remember { mutableStateOf("") }

    val animalTypes = listOf("Poultry", "Goats", "Sheep", "Cattle", "Pigs")
    val healthStatuses = listOf("Healthy", "Under Treatment", "Quarantined")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_livestock_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Log Animal Herd Record",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "Submit animal herd parameters directly to local Room repository cache.",
                    fontSize = 11.sp,
                    color = MutedClay
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Breed Name input
                OutlinedTextField(
                    value = breedName,
                    onValueChange = { breedName = it },
                    label = { Text("Breed / Breed Line Name") },
                    placeholder = { Text("e.g. Sahel Goat, Cobb 500 poultry") },
                    modifier = Modifier.fillMaxWidth().testTag("add_breed_name"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Animal class select chips
                Text("Animal Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    animalTypes.forEach { type ->
                        val selected = animalType == type
                        FilterChip(
                            selected = selected,
                            onClick = { animalType = type },
                            label = { Text(type, fontSize = 10.sp) },
                            modifier = Modifier.testTag("type_chip_$type")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Qty & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Herd Size (Heads)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("add_qty_heads"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Address") },
                        placeholder = { Text("e.g. Tamale West") },
                        modifier = Modifier.weight(1.2f).testTag("add_loc_address"),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Vaccine schedule parameters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nextVaccDate,
                        onValueChange = { nextVaccDate = it },
                        label = { Text("Next Vaccine Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f).testTag("add_next_vacc_date"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vaccineType,
                        onValueChange = { vaccineType = it },
                        label = { Text("Vaccine Required") },
                        modifier = Modifier.weight(1.2f).testTag("add_vacc_type"),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Quality/Treatment Status Select
                Text("Health Condition Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MutedClay)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    healthStatuses.forEach { item ->
                        val selected = healthStatus == item
                        FilterChip(
                            selected = selected,
                            onClick = { healthStatus = item },
                            label = { Text(item, fontSize = 10.sp) },
                            modifier = Modifier.testTag("status_chip_$item")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = customNotes,
                    onValueChange = { customNotes = it },
                    label = { Text("Observation comments (Feeding patterns, symptoms...)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("add_notes"),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("add_livestock_cancel")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantityStr.toIntOrNull() ?: 1
                            if (breedName.isNotBlank() && locationName.isNotBlank()) {
                                onConfirm(
                                    breedName,
                                    animalType,
                                    qty,
                                    locationName,
                                    nextVaccDate,
                                    vaccineType,
                                    healthStatus,
                                    customNotes
                                )
                            }
                        },
                        modifier = Modifier.testTag("add_livestock_confirm"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Log Herd")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 5: RETRO AGRIC FARMING ARCADE & CONSOLE MODE
// ---------------------------------------------------------------------------------

data class FarmingPlot(
    val id: Int,
    val name: String,
    val soilType: String = "None", // "None", "Savannah Silt", "Forest Loam", "Clayey Ridge", "Salty Coast"
    val prepProgress: Float = 0.0f, // 0.0 to 1.0 (soil till progress)
    val sownCrop: String = "None",  // "None", "Yellow Maize 🌽", "White Yam 🍠", "Sweet Cassava 🪵", "Golden Cocoa 🍫"
    val seedInGround: Boolean = false,
    val growthProgress: Float = 0.0f, // 0.0 to 1.0
    val waterLevel: Float = 1.0f,   // 0.0 to 1.0
    val nutrientLevel: Float = 1.0f, // 0.0 to 1.0
    val arePestsActive: Boolean = false,
    val isRipe: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgricArcadeTabContent(
    isOffline: Boolean,
    onNavigateToAdvisor: (String) -> Unit
) {
    // Current coin balance (Ghana Cedis GHS)
    var balanceGhs by remember { mutableStateOf(60) }
    // XP Points
    var xpEarned by remember { mutableStateOf(45) }
    // Farming Level
    val currentLevel = remember(xpEarned) {
        (xpEarned / 100) + 1
    }

    // Plots setup (2x2 grid representing 4 areas of land)
    var plots by remember {
        mutableStateOf(
            listOf(
                FarmingPlot(id = 1, name = "Plot #1 (North-West)", soilType = "None", prepProgress = 0.0f, sownCrop = "None"),
                FarmingPlot(id = 2, name = "Plot #2 (North-East)", soilType = "Savannah Silt", prepProgress = 0.4f, sownCrop = "None"),
                FarmingPlot(id = 3, name = "Plot #3 (South-West)", soilType = "Forest Loam", prepProgress = 1.0f, sownCrop = "None"),
                FarmingPlot(
                    id = 4, 
                    name = "Plot #4 (South-East)", 
                    soilType = "Clayey Ridge", 
                    prepProgress = 1.0f, 
                    sownCrop = "Yellow Maize 🌽", 
                    seedInGround = true, 
                    growthProgress = 0.35f, 
                    waterLevel = 0.75f, 
                    nutrientLevel = 0.9f
                )
            )
        )
    }

    // Selected cursor position (plot id 1..4)
    var selectedPlotId by remember { mutableStateOf(1) }
    val selectPlot = remember(plots, selectedPlotId) {
        plots.first { it.id == selectedPlotId }
    }

    // Cycled lands selection
    val soilOptions = listOf("Savannah Silt", "Forest Loam", "Clayey Ridge", "Salty Coast")
    var soilIndexSelector by remember { mutableStateOf(0) }

    // Cycled seeds selection
    val seedOptions = listOf("Yellow Maize 🌽", "White Yam 🍠", "Sweet Cassava 🪵", "Golden Cocoa 🍫")
    var seedIndexSelector by remember { mutableStateOf(0) }

    // Console logs list
    var consoleLogs by remember {
        mutableStateOf(
            listOf(
                "[CONSOLE] Welcome to Agric Console classic! Ready to plought and till.",
                "[GUIDE] Press directional D-PAD yellow buttons to cycle Plot #1 - #4.",
                "[GUIDE] Use SELECT/CYCLE to pick Soil type or Sowing seed.",
                "[GUIDE] Use BUTTON A to Execute Actions. Use BUTTON B to Spray or Water!"
            )
        )
    }

    // Append log helper
    val pushLog: (String) -> Unit = remember {
        { msg ->
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            consoleLogs = (listOf("[$timestamp] $msg") + consoleLogs).take(20)
        }
    }

    // Game loop simulation tick
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1600)
            plots = plots.map { plot ->
                if (plot.seedInGround && !plot.isRipe) {
                    val isDry = plot.waterLevel < 0.2f
                    val isStarved = plot.nutrientLevel < 0.2f
                    
                    val multiplier = when (plot.soilType) {
                        "Forest Loam" -> 1.3f
                        "Clayey Ridge" -> 0.9f
                        "Salty Coast" -> 0.7f
                        else -> 1.0f
                    }
                    val cropSpeed = when {
                        plot.sownCrop.contains("Maize") -> 0.08f
                        plot.sownCrop.contains("Yam") -> 0.05f
                        plot.sownCrop.contains("Cassava") -> 0.06f
                        plot.sownCrop.contains("Cocoa") -> 0.03f
                        else -> 0.05f
                    }

                    // Water depletion speed
                    val waterDrain = when (plot.soilType) {
                        "Clayey Ridge" -> 0.04f
                        "Salty Coast" -> 0.09f
                        else -> 0.06f
                    } * (if (plot.arePestsActive) 2.0f else 1.0f)

                    val nutrientDrain = 0.04f
                    val penalty = if (isDry) 0.3f else if (isStarved) 0.5f else 1.0f
                    val pestDivider = if (plot.arePestsActive) 0.2f else 1.0f

                    val newGrowth = plot.growthProgress + (cropSpeed * multiplier * penalty * pestDivider)
                    val reachesRipe = newGrowth >= 1.0f

                    // Pest Trigger (8% chance)
                    val triggerPests = !plot.arePestsActive && (Math.random() < 0.08)

                    if (reachesRipe && !plot.isRipe) {
                        pushLog("SUCCESS: Plot #${plot.id} ${plot.sownCrop} matured! Hit action BUTTON A to Harvest! 🌽")
                    }
                    if (triggerPests) {
                        pushLog("WARNING: Whiteflies pest damage on Plot #${plot.id}! Spray instantly with Button B!")
                    }

                    plot.copy(
                        growthProgress = newGrowth.coerceAtMost(1.0f),
                        isRipe = plot.isRipe || reachesRipe,
                        waterLevel = (plot.waterLevel - waterDrain).coerceAtLeast(0.0f),
                        nutrientLevel = (plot.nutrientLevel - nutrientDrain).coerceAtLeast(0.0f),
                        arePestsActive = plot.arePestsActive || triggerPests
                    )
                } else {
                    plot
                }
            }
        }
    }

    // BUTTON A actions handler (Action)
    val triggerActionA = remember(selectPlot, balanceGhs, soilIndexSelector, seedIndexSelector) {
        {
            when {
                selectPlot.soilType == "None" -> {
                    // Assign and buy land
                    val chosenSoil = soilOptions[soilIndexSelector]
                    val cost = when (chosenSoil) {
                        "Forest Loam" -> 35
                        "Clayey Ridge" -> 15
                        "Salty Coast" -> 10
                        else -> 0
                    }
                    if (balanceGhs >= cost) {
                        balanceGhs -= cost
                        plots = plots.map {
                            if (it.id == selectedPlotId) {
                                it.copy(soilType = chosenSoil, prepProgress = 0.0f)
                            } else it
                        }
                        pushLog("Select & Sourced $chosenSoil on Plot #$selectedPlotId. Cost: $cost GHS.")
                    } else {
                        pushLog("[ALERT] Insufficient cash! $chosenSoil needs $cost GHS.")
                    }
                }
                selectPlot.prepProgress < 1.0f -> {
                    // Prepare soil till
                    val nextPrep = (selectPlot.prepProgress + 0.25f).coerceAtMost(1.0f)
                    plots = plots.map {
                        if (it.id == selectedPlotId) {
                            it.copy(prepProgress = nextPrep)
                        } else it
                    }
                    if (nextPrep >= 1.0f) {
                        pushLog("Ploughing done on Plot #$selectedPlotId! Soil prepared. Sowing READY!")
                    } else {
                        pushLog("Till soil in progress... Readiness: ${(nextPrep * 100).toInt()}%")
                    }
                }
                !selectPlot.seedInGround -> {
                    // Sow seed
                    val chosenSeed = seedOptions[seedIndexSelector]
                    val cost = when {
                        chosenSeed.contains("Maize") -> 5
                        chosenSeed.contains("Yam") -> 12
                        chosenSeed.contains("Cassava") -> 8
                        chosenSeed.contains("Cocoa") -> 30
                        else -> 5
                    }
                    if (balanceGhs >= cost) {
                        balanceGhs -= cost
                        plots = plots.map {
                            if (it.id == selectedPlotId) {
                                it.copy(
                                    sownCrop = chosenSeed,
                                    seedInGround = true,
                                    growthProgress = 0.0f,
                                    waterLevel = 1.0f,
                                    nutrientLevel = 1.0f,
                                    arePestsActive = false,
                                    isRipe = false
                                )
                            } else it
                        }
                        pushLog("Cultivated Seed: $chosenSeed on Plot #$selectedPlotId. Spent $cost GHS.")
                    } else {
                        pushLog("[ALERT] Need $cost GHS money for $chosenSeed seeds!")
                    }
                }
                selectPlot.isRipe -> {
                    // Harvest and Sell
                    val rewardCash = when {
                        selectPlot.sownCrop.contains("Maize") -> 20
                        selectPlot.sownCrop.contains("Yam") -> 45
                        selectPlot.sownCrop.contains("Cassava") -> 32
                        selectPlot.sownCrop.contains("Cocoa") -> 115
                        else -> 10
                    }
                    balanceGhs += rewardCash
                    xpEarned += rewardCash

                    plots = plots.map {
                        if (it.id == selectedPlotId) {
                            it.copy(
                                soilType = "None",
                                prepProgress = 0.0f,
                                sownCrop = "None",
                                seedInGround = false,
                                growthProgress = 0.0f,
                                isRipe = false,
                                arePestsActive = false
                            )
                        } else it
                    }
                    pushLog("🏆 HARVESTED! Sold $rewardCash GHS of crops on Plot #$selectedPlotId. Got +$rewardCash XP!")
                }
                else -> {
                    pushLog("Crop growing... Water: ${(selectPlot.waterLevel*100).toInt()}% | Growth: ${(selectPlot.growthProgress*100).toInt()}%")
                }
            }
        }
    }

    // BUTTON B actions handler (Care: Sprayer, Irrigate, Nutrient boost)
    val triggerActionB = remember(selectPlot) {
        {
            if (!selectPlot.seedInGround) {
                pushLog("No cultivated seed on Plot #$selectedPlotId to nurse.")
            } else {
                when {
                    selectPlot.arePestsActive -> {
                        // Pest spray
                        plots = plots.map {
                            if (it.id == selectedPlotId) {
                                it.copy(arePestsActive = false)
                            } else it
                        }
                        pushLog("Sprayed neem extract insecticide. Pest infestation Cleared!")
                    }
                    selectPlot.waterLevel < 0.6f -> {
                        // Irrigate crop
                        plots = plots.map {
                            if (it.id == selectedPlotId) {
                                it.copy(waterLevel = 1.0f)
                            } else it
                        }
                        pushLog("Irrigation active. Refilled moisture on Plot #$selectedPlotId.")
                    }
                    selectPlot.nutrientLevel < 0.6f -> {
                        // Apply fertilizer
                        plots = plots.map {
                            if (it.id == selectedPlotId) {
                                it.copy(nutrientLevel = 1.0f)
                            } else it
                        }
                        pushLog("Applied NPK 15-15-15 nutrients. Crop highly vitalized!")
                    }
                    else -> {
                        pushLog("Nursing status OK! Soil is moist and nutritious. No care actions needed.")
                    }
                }
            }
        }
    }

    // SELECT Button handler
    val triggerSelectCycle = remember(selectPlot, soilIndexSelector, seedIndexSelector) {
        {
            if (selectPlot.soilType == "None") {
                soilIndexSelector = (soilIndexSelector + 1) % soilOptions.size
                pushLog("Select Soil: ${soilOptions[soilIndexSelector]} for Plot #$selectedPlotId.")
            } else if (!selectPlot.seedInGround) {
                seedIndexSelector = (seedIndexSelector + 1) % seedOptions.size
                pushLog("Select Seed: ${seedOptions[seedIndexSelector]} for Plot #$selectedPlotId.")
            } else {
                pushLog("Selection locked. Crop actively cultivating currently.")
            }
        }
    }

    // RESET Button handler
    val triggerResetPlot = remember {
        {
            plots = plots.map {
                if (it.id == selectedPlotId) {
                    FarmingPlot(id = selectedPlotId, name = "Plot #$selectedPlotId", soilType = "None", prepProgress = 0.0f, sownCrop = "None")
                } else it
            }
            pushLog("Re-cleared Plot #$selectedPlotId to bare state.")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("arcade_scroll")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portal Heading Banner
        item {
            Column {
                Text(
                    text = "Agric-Boy Color™ Emulator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "Retro physical console simulator. Prepare and clear land, till soil, select high-yield seeds, manage dynamic climate stress, and harvest crops for premium Ghana Cedis!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // PHYSICAL GAME CONSOLE EMBED
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retro_console_bezel"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4C5D4B)), // Physical dark green handheld body
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(4.dp, Color(0xFF2B3A2A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // HANDHELD LOGO HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AGRI-BOY COLOR SYSTEM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4E157),
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "POWER ON",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // RETRO DIGITAL HUD CRT SCREEN SCREEN
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(6.dp, Color(0xFF1E281C), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E0C)) // Backlit CRT terminal black-green
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // SCORE BAR HUD
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🪙 CASH: $balanceGhs GHS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFAEEA00)
                                )
                                Text(
                                    text = "⭐ LV. $currentLevel ($xpEarned XP)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF80DEEA)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFF1E3F1F), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            // 2x2 CROP MATRIX PLOTS GRID
                            Text(
                                text = "ACTIVE FARM PLOTS (USE D-PAD TO MOVE CURSOR):",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7CB342),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Column Left (Plot 1, Plot 3)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PixelPlotCard(plots[0], selectedPlotId == 1) { selectedPlotId = 1 }
                                    PixelPlotCard(plots[2], selectedPlotId == 3) { selectedPlotId = 3 }
                                }
                                // Column Right (Plot 2, Plot 4)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PixelPlotCard(plots[1], selectedPlotId == 2) { selectedPlotId = 2 }
                                    PixelPlotCard(plots[3], selectedPlotId == 4) { selectedPlotId = 4 }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFF1E3F1F), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // HIGHLIGHTED PLOT EXPANDED SCREEN HUD INFO
                            Text(
                                text = "SELECTED: PILOT ${selectPlot.id} - ${selectPlot.name}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDD835)
                            )

                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "SOIL: ${selectPlot.soilType.uppercase()}",
                                        fontSize = 9.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "CROP: ${selectPlot.sownCrop.uppercase()}",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }

                                if (selectPlot.soilType != "None" && selectPlot.prepProgress < 1.0f) {
                                    // Plowing feedback
                                    Text(
                                        text = "SOIL STATUS: Ploughing [${"❚".repeat(((selectPlot.prepProgress * 10).toInt()).coerceAtLeast(0))}${".".repeat(((10 - (selectPlot.prepProgress * 10).toInt())).coerceAtLeast(0))}] ${(selectPlot.prepProgress*100).toInt()}%",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFF8A65),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                } else if (selectPlot.seedInGround) {
                                    // Growth meters
                                    val ripeTag = if (selectPlot.isRipe) "CROP RIPE! HARVEST NOW!" else "${(selectPlot.growthProgress*100).toInt()}% READY"
                                    Text(
                                        text = "GROWTH: [${"❚".repeat(((selectPlot.growthProgress * 10).toInt()).coerceAtLeast(0))}${".".repeat(((10 - (selectPlot.growthProgress * 10).toInt())).coerceAtLeast(0))}] $ripeTag",
                                        fontSize = 9.sp,
                                        color = if (selectPlot.isRipe) Color(0xFFAEEA00) else Color(0xFF81C784)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "MOISTURE: [${"❚".repeat(((selectPlot.waterLevel*5).toInt()).coerceAtLeast(0))}] ${(selectPlot.waterLevel*100).toInt()}%",
                                            fontSize = 8.sp,
                                            color = if (selectPlot.waterLevel < 0.3f) Color.Red else Color(0xFF64B5F6)
                                        )
                                        Text(
                                            text = "NPK FEED: [${"❚".repeat(((selectPlot.nutrientLevel*5).toInt()).coerceAtLeast(0))}] ${(selectPlot.nutrientLevel*100).toInt()}%",
                                            fontSize = 8.sp,
                                            color = if (selectPlot.nutrientLevel < 0.3f) Color.Red else Color(0xFFFFB74D)
                                        )
                                    }
                                    if (selectPlot.arePestsActive) {
                                        Text(
                                            text = "🪲 PEST RISK HIGH: BLIGHT INVASION SLOWS GROWTH 5X",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                } else if (selectPlot.soilType != "None") {
                                    Text(
                                        text = "SOIL STATUS: HIGH-YIELD READY. PRESS SELECT BUTTON TO CHOICE SEED CORES.",
                                        fontSize = 8.sp,
                                        color = Color(0xFFC0CA33),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                } else {
                                    Text(
                                        text = "SOIL STATUS: UNOWNED. PRESS SELECT TO CYCLE LAND SOURCE TYPES.",
                                        fontSize = 8.sp,
                                        color = Color(0xFFB0BEC5),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // RETRO PHYSICAL KEYBOARD CONSOLE HARDWARE (D-Pad, Systems, Action keys)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DIRECTIONAL D-PAD CONTROLLER MATRIX (LEFT)
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp))
                                .background(Color(0xFF37474F)), // Outer rubber pad
                            contentAlignment = Alignment.Center
                        ) {
                            // 3x3 Grid representing high contrast D-Pad cross arrows
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxSize().padding(2.dp)
                            ) {
                                // Up button
                                DpadArrowButton("▲", "UP") {
                                    selectedPlotId = if (selectedPlotId > 2) selectedPlotId - 2 else selectedPlotId
                                    pushLog("Navigate D-PAD: Plot #$selectedPlotId")
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Button
                                    DpadArrowButton("◀", "LEFT") {
                                        selectedPlotId = if (selectedPlotId % 2 == 0) selectedPlotId - 1 else selectedPlotId
                                        pushLog("Navigate D-PAD: Plot #$selectedPlotId")
                                    }
                                    
                                    // Centered core
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E272C))
                                    )
                                    
                                    // Right Button
                                    DpadArrowButton("▶", "RIGHT") {
                                        selectedPlotId = if (selectedPlotId % 2 != 0) selectedPlotId + 1 else selectedPlotId
                                        pushLog("Navigate D-PAD: Plot #$selectedPlotId")
                                    }
                                }
                                
                                // Down button
                                DpadArrowButton("▼", "DOWN") {
                                    selectedPlotId = if (selectedPlotId <= 2) selectedPlotId + 2 else selectedPlotId
                                    pushLog("Navigate D-PAD: Plot #$selectedPlotId")
                                }
                            }
                        }

                        // RETRO SYSTEM PILLS (SELECT & RESET)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // SELECT BUTTON
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1F292E))
                                        .clickable { triggerSelectCycle() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CYCLE", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 8.sp
                                    )
                                }
                                Text("SELECT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                            }

                            // RESET STARTER BUTTON
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF880E4F)) // Deep berry red rubber
                                        .clickable { triggerResetPlot() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "RESET", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 8.sp
                                    )
                                }
                                Text("START", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        // ACTION BUTTONS A & B (RIGHT)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Button B (Nursing Care Actions)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0xFFC62828)) // Tactile red cap
                                        .clickable { triggerActionB() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Text("CARE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                            }

                            // Button A (Farming Actions)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF2E7D32)) // Tactile green cap
                                        .clickable { triggerActionA() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Text("ACTION", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // HELP GUIDE & STRATEGY PANEL
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AGRIC SIMULATION GAME GUIDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedClay,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Selecting Land: Move cursor to empty plot. Press CYCLE to pick land soil. Press A to Settle & Source.\n" +
                               "2. Prepare soil: Soil ready to till is represented by 🟫. Press A several times (till soil readiness reaches 100%).\n" +
                               "3. Sowing: Soil ready for seed is 🌾. Press CYCLE to toggle high-yield crop seeds: Cocoa 🍫, Maize 🌽, Yam 🍠, Cassava 🪵. Press A to Plant.\n" +
                               "4. Care nursing: Ensure moisture & nutrients are high. If whiteflies infest (🪲), hit Button B for pesticide spray.\n" +
                               "5. Harvesting: When mature, the crop shows RIPE. Press Button A to Sell instantly at local market rates!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Live Market Reference Link
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HighDensityBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Consult AI Advisory on Best Crop Rotation?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Text(
                            text = "Transfer virtual arcade agricultural learnings into the offline intelligence portal stream.",
                            fontSize = 10.sp,
                            color = MutedClay
                        )
                    }
                    Button(
                        onClick = {
                            onNavigateToAdvisor("Tell me about best practices for crop rotation of Cocoa, Maize, and Cassava in Ashanti Region loam soil.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Ask Advisor", fontSize = 11.sp)
                    }
                }
            }
        }

        // REAL-TIME 8-BIT ACTION LOG STREAM
        item {
            Text(
                text = "Agri-Console Stream Log Buffer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
        }

        items(consoleLogs) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F3F4))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = log,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = if (log.contains("SUCCESS") || log.contains("HARVESTED")) Color(0xFF2E7D32) 
                            else if (log.contains("WARNING") || log.contains("ALERT") || log.contains("ERROR")) Color(0xFFC62828)
                            else Color(0xFF37474F)
                )
            }
        }
    }
}

@Composable
fun PixelPlotCard(
    plot: FarmingPlot,
    isCursorSelected: Boolean,
    onSelect: () -> Unit
) {
    val outlineColor = if (isCursorSelected) Color(0xFFFDD835) else Color(0xFF1E3F1F)
    val capTextColor = if (isCursorSelected) Color(0xFFFDD835) else Color(0xFF81C784)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(2.dp, outlineColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCursorSelected) Color(0xFF152A12) else Color(0xFF0C160B))
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plot visual icon emitter
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1A3317)),
                contentAlignment = Alignment.Center
            ) {
                val iconStr = when {
                    plot.soilType == "None" -> "🌫️"
                    plot.prepProgress < 1.0f -> "🟫"
                    !plot.seedInGround -> "🌾"
                    plot.isRipe -> "🌽"
                    plot.sownCrop.contains("Maize") -> "🌱"
                    plot.sownCrop.contains("Yam") -> "🌿"
                    plot.sownCrop.contains("Cassava") -> "🪴"
                    plot.sownCrop.contains("Cocoa") -> "🌳"
                    else -> "🌱"
                }
                Text(text = iconStr, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = "PLOT #${plot.id}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = capTextColor
                )
                val descStr = when {
                    plot.soilType == "None" -> "Unowned Land"
                    plot.prepProgress < 1.0f -> "Plowing ${(plot.prepProgress*100).toInt()}%"
                    !plot.seedInGround -> "Prepared soil"
                    plot.isRipe -> "Ripe ${plot.sownCrop.substringBefore(" ")}"
                    else -> "${(plot.growthProgress*100).toInt()}% Ready"
                }
                Text(
                    text = descStr,
                    fontSize = 9.sp,
                    color = if (plot.arePestsActive) Color.Red else Color.White
                )
            }
        }
    }
}

@Composable
fun DpadArrowButton(
    arrow: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFBC02D)) // High visibility arcade yellow
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = arrow,
            color = Color(0xFF1E272C),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}


