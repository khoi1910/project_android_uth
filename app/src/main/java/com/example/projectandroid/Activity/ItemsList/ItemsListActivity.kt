package com.example.projectandroid.Activity.ItemsList

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.runtime.livedata.observeAsState
import com.example.ViewModel.MainViewModel
import com.example.projectandroid.R
import com.example.projectandroid.Activity.ItemsList.ItemsList

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

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        ConstraintLayout(
            modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)
        ) {
            val (backBtn, cartTxt) = createRefs()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) { centerTo(parent) },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                text = title
            )
            Image(
                painter = painterResource(R.drawable.back_grey),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
        }

        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { androidx.compose.material3.Text(text = "Search food...") },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.orange),
                unfocusedBorderColor = colorResource(id = R.color.grey),
                cursorColor = colorResource(id = R.color.orange),
                focusedPlaceholderColor = colorResource(id = R.color.grey),
                unfocusedPlaceholderColor = colorResource(id = R.color.grey)
            )
        )

        // Content
        when {
            isLoading && !hasTimedOut -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Text(
                            text = "Loading $title...",
                            fontSize = 16.sp,
                            color = colorResource(R.color.darkPurple)
                        )
                    }
                }
            }
            filteredItems.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Text(
                            text = "No items found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.darkPurple)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.Text(
                            text = "No food matches your search",
                            fontSize = 14.sp,
                            color = colorResource(R.color.darkPurple),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Text(
                            text = "Category ID: $id",
                            fontSize = 12.sp,
                            color = colorResource(R.color.darkPurple),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                ItemsList(filteredItems)
            }
        }
    }
}