package com.example.projectandroid.Activity.Cart

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.projectandroid.R
import com.example.projectandroid.Helper.ManagmentCart
import com.example.projectandroid.Helper.NetworkUtils
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen(
                managmentCart = ManagmentCart(this),
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun CartScreen(
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val cartItem = remember { mutableStateOf(managmentCart.getListCart()) }
    val tax = remember { mutableStateOf(0.0) }
    calculatorCart(managmentCart, tax)

    LaunchedEffect(isNetworkAvailable) {
        if (!isNetworkAvailable) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "You are in offline mode, cannot perform cart actions",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
                val (backBtn, cartTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    text = "Your Cart",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Image(
                    painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .clickable { onBackClick() }
                )
            }
        }

        if (cartItem.value.isEmpty()) {
            item {
                Text(
                    text = "Cart Is Empty",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(cartItem.value) { item ->
                CartItem(
                    cartItems = cartItem.value,
                    item = item,
                    managmentCart = managmentCart,
                    onItemChange = {
                        if (isNetworkAvailable) {
                            calculatorCart(managmentCart, tax)
                            cartItem.value = ArrayList(managmentCart.getListCart())
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "You are in offline mode, cannot modify the cart",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }

            item {
                Text(
                    text = "Order Summary",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                CartSummary(
                    itemTotal = managmentCart.getTotalFee(),
                    tax = tax.value,
                    delivery = 10.0
                )
            }

            item {
                Text(
                    text = "Information",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                DeliveryInfoBox()
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

fun calculatorCart(managmentCart: ManagmentCart, tax: MutableState<Double>) {
    val percentTax = 0.02
    tax.value = Math.round((managmentCart.getTotalFee() * percentTax) * 100) / 100.0
}