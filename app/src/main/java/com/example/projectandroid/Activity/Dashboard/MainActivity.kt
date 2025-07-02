package com.example.projectandroid.Activity.Dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ViewModel.MainViewModel
import com.example.projectandroid.Activity.BaseActivity
import com.example.projectandroid.Activity.Splash.SplashActivity
import com.example.projectandroid.Components.TopBar
import com.example.projectandroid.Domain.BannerModel
import com.example.projectandroid.Domain.CategoryModel
import com.example.projectandroid.Helper.AuthManager
import com.example.projectandroid.activity.dashboard.CategorySection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kiểm tra trạng thái đăng nhập
        val authManager = AuthManager(this)

        // Nếu chưa đăng nhập hoặc token hết hạn -> chuyển đến SplashActivity
        if (!authManager.isLoggedIn()) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }

        // Đã đăng nhập và token còn hạn -> tiếp tục hiển thị MainActivity
        // Refresh token trong background để duy trì session
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = result.token
            if (token != null) {
                authManager.saveAuthToken(token)
            }
        }?.addOnFailureListener {
            // Nếu refresh token thất bại, có thể token đã hết hạn
            // Chuyển về splash để đăng nhập lại
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return@addOnFailureListener
        }

        // Hiển thị MainActivity
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val scaffoldState = rememberScaffoldState()
    val viewModel = MainViewModel()

    val banners = remember { mutableStateListOf<BannerModel>() }
    val categories = remember { mutableStateListOf<CategoryModel>() }

    var showBannerLoading by remember { mutableStateOf(true) }
    var showCategoryLoading by remember { mutableStateOf(true) }

    // Firebase auth & display name
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var displayName by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    displayName = doc.getString("displayName") ?: user.displayName ?: ""
                }
                .addOnFailureListener {
                    displayName = user.displayName ?: ""
                }
        }
    }

    // Load banners
    LaunchedEffect(Unit) {
        viewModel.loadBanner().observeForever {
            banners.clear()
            banners.addAll(it)
            showBannerLoading = false
        }
    }

    // Load categories
    LaunchedEffect(Unit) {
        viewModel.loadCategory().observeForever {
            categories.clear()
            categories.addAll(it)
            showCategoryLoading = false
        }
    }

    Scaffold(
        bottomBar = { MyBottomBar() },
        scaffoldState = scaffoldState
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                TopBar(displayName = displayName)
            }
            item {
                Banner(banners, showBannerLoading)
            }
            item {
                CategorySection(categories, showCategoryLoading)
            }
        }
    }
}