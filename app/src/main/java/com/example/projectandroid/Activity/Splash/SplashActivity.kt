package com.example.projectandroid.Activity.Splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projectandroid.Activity.BaseActivity
import com.example.projectandroid.Activity.Dashboard.MainActivity
import com.example.projectandroid.Helper.AuthManager
import com.example.projectandroid.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val authManager = AuthManager(this)
        val auth = FirebaseAuth.getInstance()

        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }
            var checkingAuth by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                // Kiểm tra trạng thái đăng nhập
                isLoggedIn = authManager.isLoggedIn()

                if (isLoggedIn) {
                    // Đã đăng nhập và token còn hạn -> refresh token và chuyển về Main
                    auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                        val token = result.token
                        if (token != null) {
                            authManager.saveAuthToken(token)
                            // Chuyển về MainActivity
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        } else {
                            // Token không hợp lệ -> hiện splash để đăng nhập
                            checkingAuth = false
                            showSplash = true
                        }
                    }?.addOnFailureListener {
                        // Lỗi khi refresh token -> hiện splash để đăng nhập
                        checkingAuth = false
                        showSplash = true
                    }
                } else {
                    // Chưa đăng nhập -> hiện splash sau delay
                    delay(2000) // Hiện splash trong 2 giây
                    checkingAuth = false
                    showSplash = true
                }
            }

            // Chỉ hiện splash screen khi cần thiết
            if (showSplash && !checkingAuth) {
                SplashScreenContent(
                    onGetStartedClick = {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    },
                    onSignUpClick = {
                        startActivity(Intent(this@SplashActivity, SignUpActivity::class.java))
                    }
                )
            }
            // Khi đang check auth, có thể hiện loading hoặc màn hình trống
        }
    }
}

@Composable
@Preview
fun SplashScreenContent(
    onGetStartedClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.darkBrown))
    ) {
        ConstraintLayout(modifier = Modifier.padding(top = 48.dp)) {
            val (backgroundImg, logoImg) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.intro_pic),
                contentDescription = null,
                modifier = Modifier
                    .constrainAs(backgroundImg) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(R.drawable.pizza),
                contentDescription = null,
                modifier = Modifier.constrainAs(logoImg) {
                    top.linkTo(backgroundImg.top)
                    bottom.linkTo(backgroundImg.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                contentScale = ContentScale.Fit
            )
        }

        val styledText = buildAnnotatedString {
            append("Welcome to your ")
            withStyle(style = SpanStyle(color = colorResource(R.color.orange))) {
                append("food\nparadise ")
            }
            append("experience food perfection delivered")
        }

        Text(
            text = styledText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(top = 32.dp, start = 16.dp, end = 16.dp)
        )

        Text(
            text = stringResource(R.string.splashSubtitle),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sign Up button on the left
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Sign Up", fontWeight = FontWeight.SemiBold)
            }

            // Sign In button on the right
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.orange))
            ) {
                Text("Sign In", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}