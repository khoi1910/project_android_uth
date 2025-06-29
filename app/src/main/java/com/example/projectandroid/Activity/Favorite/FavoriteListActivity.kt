package com.example.projectandroid.Activity.Favorite

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.projectandroid.R
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState

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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf(Double.MAX_VALUE) }
    var minRating by remember { mutableStateOf(0.0) }

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
        it.Price >= minPrice && it.Price <= maxPrice && it.Star >= minRating
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
                        onClick = onBackClick,
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
                        onClick = { isSearchExpanded = !isSearchExpanded },
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
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = {
                            Text(
                                text = "🔍 Search favorite items...",
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
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
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

            // Beautiful Sort Options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = colorResource(id = R.color.orange),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sort by Price",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.darkPurple)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SortChip(
                            text = "Default",
                            icon = Icons.Default.Close,
                            isSelected = sortOrder == SortOrder.NONE,
                            onClick = { sortOrder = SortOrder.NONE }
                        )
                        SortChip(
                            text = "Low to High",
                            icon = Icons.Default.KeyboardArrowUp,
                            isSelected = sortOrder == SortOrder.PRICE_ASC,
                            onClick = { sortOrder = SortOrder.PRICE_ASC }
                        )
                        SortChip(
                            text = "High to Low",
                            icon = Icons.Default.KeyboardArrowDown,
                            isSelected = sortOrder == SortOrder.PRICE_DESC,
                            onClick = { sortOrder = SortOrder.PRICE_DESC }
                        )
                    }
                }
            }

            // Advanced Filter UI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Advanced Filters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.darkPurple),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
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
                                minPrice = it.toDoubleOrNull() ?: 0.0
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
                            value = if (maxPrice == Double.MAX_VALUE) "" else maxPrice.toString(),
                            onValueChange = {
                                maxPrice = it.toDoubleOrNull() ?: Double.MAX_VALUE
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
                            minRating = it.toDoubleOrNull() ?: 0.0
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

            Scaffold(
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
                                    onRemoveItem = onRemoveItem
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
    onRemoveItem: (FoodModel) -> Unit
) {
    val context = LocalContext.current
    val isEvenRow = index % 2 == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(
                    context,
                    DetailEachFoodActivity::class.java
                ).apply {
                    putExtra("object", item)
                }
                startActivity(context, intent, null)
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
                DeleteButton(item = item, onRemoveItem = onRemoveItem)
            } else {
                DeleteButton(item = item, onRemoveItem = onRemoveItem)
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