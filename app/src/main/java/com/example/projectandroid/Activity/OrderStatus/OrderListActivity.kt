package com.example.projectandroid.Activity.OrderStatus

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.projectandroid.Domain.OrderModel
import com.example.projectandroid.Helper.ManagmentOrderFirestore
import com.example.projectandroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderListActivity : AppCompatActivity() {
    private lateinit var viewModel: ManagmentOrderFirestore
    private val userId = "userId_placeholder" // TODO: Lấy userId thực tế từ Auth hoặc lưu trữ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ManagmentOrderFirestore(this, userId)

        setContent {
            var orders by remember { mutableStateOf(listOf<OrderModel>()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    orders = viewModel.getPaidOrders()
                }
            }

            OrderListScreen(
                orders = orders,
                onOrderClick = { order ->
                    val intent = Intent(this, OrderDetailActivity::class.java)
                    intent.putExtra("order", order)
                    startActivity(intent)
                },
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun OrderListScreen(
    orders: List<OrderModel>,
    onOrderClick: (OrderModel) -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, bottom = 24.dp)
                ) {
                    val (backBtn, titleTxt) = createRefs()

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .constrainAs(titleTxt) {
                                centerHorizontallyTo(parent)
                                centerVerticallyTo(parent)
                            },
                        text = "Order List",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = colorResource(R.color.darkPurple)
                    )

                    Image(
                        painter = painterResource(R.drawable.back_grey),
                        contentDescription = "Back",
                        modifier = Modifier
                            .constrainAs(backBtn) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                            }
                            .clickable { onBackClick() }
                            .padding(4.dp)
                    )
                }
            }

            if (orders.isEmpty()) {
                item {
                    Text(
                        text = "No orders found",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            } else {
                items(orders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onOrderClick(order) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order ID: ${order.orderId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colorResource(R.color.darkPurple)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(order.orderDate))}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total: $${String.format("%.2f", order.totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }
    }
}