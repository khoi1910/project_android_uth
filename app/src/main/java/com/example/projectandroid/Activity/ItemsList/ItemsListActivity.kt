package com.example.projectandroid.Activity.ItemsList

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ViewModel.MainViewModel
import com.example.projectandroid.R

// Define SortOrder enum
enum class SortOrder {
    NONE,
    PRICE_ASC,
    PRICE_DESC
}

class ItemsListActivity : AppCompatActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""

        Log.d("ItemsListActivity", "Category ID: $id, Title: $title")

        setContent {
            ItemsListScreen(
                title = title,
                onBackClick = { finish() },
                viewModel = viewModel,
                id = id
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun ItemsListScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: MainViewModel,
    id: String
) {
    val items by viewModel.loadFiltered(id).observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var hasTimedOut by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf<Double?>(null) }
    var minRating by remember { mutableStateOf(0.0) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortExpanded by remember { mutableStateOf(false) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    // Animations
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorResource(id = R.color.orange).copy(alpha = 0.1f),
            Color.White,
            colorResource(id = R.color.orange).copy(alpha = 0.05f)
        )
    )

    // Reset loading state when id changes
    LaunchedEffect(id) {
        isLoading = true
        hasTimedOut = false
        Log.d("ItemsListScreen", "Loading started for category: $id")
    }

    // Update loading state based on data
    LaunchedEffect(items) {
        Log.d("ItemsListScreen", "Items received: ${items.size}")
        if (items.isNotEmpty()) {
            isLoading = false
        }
    }

    // Timeout after 5 seconds
    LaunchedEffect(id) {
        kotlinx.coroutines.delay(5000)
        if (items.isEmpty()) {
            hasTimedOut = true
            Log.d("ItemsListScreen", "Loading timed out for category: $id")
        }
        isLoading = false
    }

    val filteredItems = if (searchQuery.isEmpty()) {
        items
    } else {
        items.filter { it.Title.contains(searchQuery, ignoreCase = true) }
    }

    val advancedFilteredItems = filteredItems.filter {
        val maxPriceValue = maxPrice ?: Double.MAX_VALUE
        it.Price >= minPrice && it.Price <= maxPriceValue && it.Star >= minRating
    }

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
                        text = title,
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
                                text = "Search delicious food...",
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
                                        imageVector = Icons.Default.Clear,
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
                            isSortExpanded = !isSortExpanded
                            if (isSortExpanded) isFilterExpanded = false
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Filter Button
                    FilterButton(
                        text = "Advanced Filter",
                        icon = Icons.Default.FilterList,
                        isExpanded = isFilterExpanded,
                        onClick = {
                            isFilterExpanded = !isFilterExpanded
                            if (isFilterExpanded) isSortExpanded = false
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
                                    icon = Icons.Default.Clear,
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
                                    value = maxPrice?.toString() ?: "",
                                    onValueChange = {
                                        maxPrice = if (it.isEmpty()) null else it.toDoubleOrNull()
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
                }
            }

            // Content with Animation
            AnimatedContent(
                targetState = when {
                    isLoading && !hasTimedOut -> "loading"
                    sortedItems.isEmpty() -> "empty"
                    else -> "content"
                },
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) with
                            fadeOut(animationSpec = tween(300))
                }
            ) { state ->
                when (state) {
                    "loading" -> LoadingScreen(title)
                    "empty" -> EmptyScreen(id)
                    else -> {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            ItemsList(sortedItems)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterButton(
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
private fun SortChip(
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
private fun LoadingScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colorResource(id = R.color.orange).copy(alpha = 0.3f),
                                    colorResource(id = R.color.orange).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.orange),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Loading $title...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.darkPurple)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please wait while we fetch delicious items",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyScreen(id: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
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
                        text = "🍽️",
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No Items Found",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.darkPurple),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "We couldn't find any delicious food matching your search criteria. Try adjusting your filters or search terms.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category ID: $id",
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}