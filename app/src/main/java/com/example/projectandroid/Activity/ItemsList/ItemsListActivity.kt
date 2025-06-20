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
                        Text(
                            text = "Loading $title...",
                            fontSize = 16.sp,
                            color = colorResource(R.color.darkPurple)
                        )
                    }
                }
            }
            items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No items found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.darkPurple)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This category is empty or no data available",
                            fontSize = 14.sp,
                            color = colorResource(R.color.darkPurple),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
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
                ItemsList(items)
            }
        }
    }
}