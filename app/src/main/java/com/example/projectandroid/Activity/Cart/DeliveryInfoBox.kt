package com.example.projectandroid.Activity.Cart

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectandroid.Helper.ManagmentCart
import com.example.projectandroid.Helper.ManagmentOrder
import com.example.projectandroid.Helper.ManagmentOrderFirestore
import com.example.projectandroid.Domain.OrderModel
import com.example.projectandroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun DeliveryInfoBox(){
    val context = LocalContext.current
    val managmentCart = ManagmentCart(context)
    val managmentOrder = ManagmentOrder(context)
    val userId = "userId_placeholder" // TODO: Lấy userId thực tế từ Auth hoặc lưu trữ
    val managmentOrderFirestore = ManagmentOrderFirestore(context, userId)
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Delivery Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            InfoItem(
                title = "Your Delivery Address",
                content = "UTH",
                icon = painterResource(R.drawable.location)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoItem(
                title = "Payment Method",
                content = "Payment with QR Code",
                icon = painterResource(R.drawable.credit_card)
            )
        }

        // Place Order Button
        Button(
            onClick = {
                try {
                    val cartItems = managmentCart.getListCart()
                    if (cartItems.isNotEmpty()) {
                        // Tạo OrderModel mới
                        val order = OrderModel(
                            orderId = UUID.randomUUID().toString(),
                            orderDate = System.currentTimeMillis(),
                            totalAmount = managmentCart.getTotalFee(),
                            items = cartItems,
                            status = "Completed"
                        )

                        // Lưu đơn hàng lên Firestore và bộ nhớ cục bộ
                        coroutineScope.launch {
                            managmentOrderFirestore.savePaidOrder(order)
                            managmentOrder.saveOrder(order)
                        }

                        // Xóa giỏ hàng cục bộ và Firestore
                        managmentCart.clearCart()
                        coroutineScope.launch {
                            // Nếu có ManagmentCartFirestore, xóa giỏ hàng Firestore ở đây
                        }

                        // Hiển thị thông báo thành công
                        Toast.makeText(context, "Payment successful", Toast.LENGTH_SHORT).show()

                        // Chuyển sang OrderListActivity
                        val intent = Intent(context, com.example.projectandroid.Activity.OrderStatus.OrderListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)

                        // Nếu context là Activity, có thể finish để user không quay lại cart trống
                        if (context is androidx.activity.ComponentActivity) {
                            context.finish()
                        }
                    } else {
                        Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error processing order: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.orange)
            ),
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Place Order",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun InfoItem(title: String, content: String, icon: Painter) {
    Column {
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = icon,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = content,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}