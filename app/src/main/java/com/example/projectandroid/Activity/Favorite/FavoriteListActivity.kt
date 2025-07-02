package com.example.projectandroid.Activity.Favorite

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.projectandroid.Activity.DetailEachFood.DetailEachFoodActivity
import com.example.projectandroid.Activity.ItemsList.FoodDetails
import com.example.projectandroid.Activity.ItemsList.FoodImage
import com.example.projectandroid.Domain.FoodModel
import com.example.projectandroid.Helper.ManagmentFavorite
import com.example.projectandroid.Helper.NetworkUtils
import com.example.projectandroid.R
import kotlinx.coroutines.launch

// Define SortOrder enum
enum class SortOrder {
    NONE,
    PRICE_ASC,
    PRICE_DESC
}

class FavoriteListActivity : AppCompatActivity() {
    private lateinit var managmentFavorite: ManagmentFavorite
    private var favoriteItems = mutableStateListOf<FoodModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        managmentFavorite = ManagmentFavorite(this)
        favoriteItems.addAll(managmentFavorite.getListFavorite())

        setContent {
            FavoriteListScreen(
                favoriteItems = favoriteItems,
                onBackClick = { finish() },
                onRemoveItem = { item ->
                    managmentFavorite.removeFavorite(item)
                    favoriteItems.remove(item)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FavoriteListScreen(
    favoriteItems: List<FoodModel>,
    onBackClick: () -> Unit,
    onRemoveItem: (FoodModel) -> Unit
) {
    val context = LocalContext.current
    val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf<Double?>(null) }
    var minRating by remember { mutableStateOf(0.0) }
    var isSortExpanded by remember { mutableStateOf(false) } // New state for sort
    var isFilterExpanded by remember { mutableStateOf(false) } // New state for filter


    // Show offline warning if network is unavailable
    LaunchedEffect(isNetworkAvailable) {
        if (!isNetworkAvailable) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "You are in offline mode, only cached data can be viewed",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorResource(id = R.color.orange).copy(alpha = 0.1f),
            Color.White,
            colorResource(id = R.color.orange).copy(alpha = 0.05f)
        )
    )

    // Filter items based on search query
    val filteredItems = if (searchQuery.isEmpty()) {
        favoriteItems
    } else {
        favoriteItems.filter { it.Title.contains(searchQuery, ignoreCase = true) }
    }

    // Apply advanced filters (price and rating)
    val advancedFilteredItems = filteredItems.filter {
        val maxPriceValue = maxPrice ?: Double.MAX_VALUE
        it.Price >= minPrice && it.Price <= maxPriceValue && it.Star >= minRating
    }

    // Sort items based on sortOrder
    val sortedItems = when (sortOrder) {
        SortOrder.PRICE_ASC -> advancedFilteredItems.sortedBy { it.Price }
        SortOrder.PRICE_DESC -> advancedFilteredItems.sortedByDescending { it.Price }
        else -> advancedFilteredItems
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header with Glass Effect
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button
                    IconButton(
                        onClick = {
                            if (isNetworkAvailable) {
                                onBackClick()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are in offline mode, cannot perform actions",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colorResource(id = R.color.orange).copy(alpha = 0.2f),
                                        colorResource(id = R.color.orange).copy(alpha = 0.1f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.orange),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "Favorite Items",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.darkPurple),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Search Toggle Button
                    IconButton(
                        onClick = {
                            if (isNetworkAvailable) {
                                isSearchExpanded = !isSearchExpanded
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are in offline mode, cannot perform actions",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colorResource(id = R.color.orange).copy(alpha = 0.2f),
                                        colorResource(id = R.color.orange).copy(alpha = 0.1f)
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colorResource(id = R.color.orange),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Animated Search Bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            if (isNetworkAvailable) {
                                searchQuery = it
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are in offline mode, cannot perform actions",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = {
                            Text(
                                text = "Search favorite items...",
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colorResource(id = R.color.orange)
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = {
                                    if (isNetworkAvailable) {
                                        searchQuery = ""
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "You are in offline mode, cannot perform actions",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear, // Changed to Clear icon
                                        contentDescription = "Clear",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.orange),
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = colorResource(id = R.color.orange)
                        )
                    )
                }
            }

            // Compact Sort and Filter Row
            Column {
                // Compact Row with Sort and Filter buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sort Button
                    FilterButton(
                        text = "Sort by Price",
                        icon = Icons.Default.List,
                        isExpanded = isSortExpanded,
                        onClick = {
                            if (isNetworkAvailable) {
                                isSortExpanded = !isSortExpanded
                                if (isSortExpanded) isFilterExpanded = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are in offline mode, cannot perform actions",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Filter Button
                    FilterButton(
                        text = "Advanced Filter",
                        icon = Icons.Default.FilterList, // Changed to FilterList icon
                        isExpanded = isFilterExpanded,
                        onClick = {
                            if (isNetworkAvailable) {
                                isFilterExpanded = !isFilterExpanded
                                if (isFilterExpanded) isSortExpanded = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "You are in offline mode, cannot perform actions",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Animated Sort Options
                AnimatedVisibility(
                    visible = isSortExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SortChip(
                                    text = "Default",
                                    icon = Icons.Default.Clear, // Changed to Clear icon
                                    isSelected = sortOrder == SortOrder.NONE,
                                    onClick = {
                                        if (isNetworkAvailable) {
                                            sortOrder = SortOrder.NONE
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "You are in offline mode, cannot perform actions",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                )
                                SortChip(
                                    text = "Low to High",
                                    icon = Icons.Default.KeyboardArrowUp,
                                    isSelected = sortOrder == SortOrder.PRICE_ASC,
                                    onClick = {
                                        if (isNetworkAvailable) {
                                            sortOrder = SortOrder.PRICE_ASC
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "You are in offline mode, cannot perform actions",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                )
                                SortChip(
                                    text = "High to Low",
                                    icon = Icons.Default.KeyboardArrowDown,
                                    isSelected = sortOrder == SortOrder.PRICE_DESC,
                                    onClick = {
                                        if (isNetworkAvailable) {
                                            sortOrder = SortOrder.PRICE_DESC
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "You are in offline mode, cannot perform actions",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Animated Filter Options
                AnimatedVisibility(
                    visible = isFilterExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Price Range",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.darkPurple),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (minPrice == 0.0) "" else minPrice.toString(),
                                    onValueChange = {
                                        if (isNetworkAvailable) {
                                            minPrice = it.toDoubleOrNull() ?: 0.0
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "You are in offline mode, cannot perform actions",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    label = { Text("Min") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(id = R.color.orange),
                                        unfocusedBorderColor = colorResource(id = R.color.grey),
                                        cursorColor = colorResource(id = R.color.orange)
                                    )
                                )
                                OutlinedTextField(
                                    value = maxPrice?.toString() ?: "",
                                    onValueChange = {
                                        if (isNetworkAvailable) {
                                            maxPrice = if (it.isEmpty()) null else it.toDoubleOrNull()
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "You are in offline mode, cannot perform actions",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    label = { Text("Max") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(id = R.color.orange),
                                        unfocusedBorderColor = colorResource(id = R.color.grey),
                                        cursorColor = colorResource(id = R.color.orange)
                                    )
                                )
                            }
                            Text(
                                text = "Minimum Rating",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.darkPurple),
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = if (minRating == 0.0) "" else minRating.toString(),
                                onValueChange = {
                                    if (isNetworkAvailable) {
                                        minRating = it.toDoubleOrNull() ?: 0.0
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "You are in offline mode, cannot perform actions",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                label = { Text("Rating") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(id = R.color.orange),
                                    unfocusedBorderColor = colorResource(id = R.color.grey),
                                    cursorColor = colorResource(id = R.color.orange)
                                )
                            )
                        }
                    }
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                content = { paddingValues ->
                    if (sortedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    colorResource(id = R.color.orange).copy(alpha = 0.2f),
                                                    colorResource(id = R.color.orange).copy(alpha = 0.05f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "❤️",
                                        fontSize = 36.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "No Favorite Items",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.darkPurple)
                                )
                                Text(
                                    text = "You haven't added any items to favorites yet",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            itemsIndexed(sortedItems) { index, item ->
                                FavoriteItem(
                                    item = item,
                                    index = index,
                                    onRemoveItem = onRemoveItem,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterButton( // Made private to match ItemsListActivity.kt
    text: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                colorResource(id = R.color.orange).copy(alpha = 0.1f)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorResource(id = R.color.orange),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.darkPurple)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colorResource(id = R.color.orange),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

@Composable
fun SortChip(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .scale(animatedScale)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                colorResource(id = R.color.orange)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else colorResource(id = R.color.orange),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else colorResource(id = R.color.darkPurple)
            )
        }
    }
}

@Composable
fun FavoriteItem(
    item: FoodModel,
    index: Int,
    onRemoveItem: (FoodModel) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
    val coroutineScope = rememberCoroutineScope()
    val isEvenRow = index % 2 == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (isNetworkAvailable) {
                    val intent = Intent(
                        context,
                        DetailEachFoodActivity::class.java
                    ).apply {
                        putExtra("object", item)
                    }
                    startActivity(context, intent, null)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "You are in offline mode, cannot perform actions",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.grey)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEvenRow) {
                FoodImage(item = item)
                FoodDetails(item = item)
                DeleteButton(
                    item = item,
                    onRemoveItem = {
                        if (isNetworkAvailable) {
                            onRemoveItem(it)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "You are in offline mode, cannot perform actions",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            } else {
                DeleteButton(
                    item = item,
                    onRemoveItem = {
                        if (isNetworkAvailable) {
                            onRemoveItem(it)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "You are in offline mode, cannot perform actions",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
                FoodDetails(item = item)
                FoodImage(item = item)
            }
        }
    }
}

@Composable
fun DeleteButton(
    item: FoodModel,
    onRemoveItem: (FoodModel) -> Unit
) {
    IconButton(
        onClick = { onRemoveItem(item) },
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colorResource(id = R.color.orange).copy(alpha = 0.2f),
                        colorResource(id = R.color.orange).copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove from Favorites",
            tint = colorResource(id = R.color.darkPurple),
            modifier = Modifier.size(20.dp)
        )
    }
}