package com.example.projectandroid.Activity.Cart

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity // Đây là AppCompatActivity, không phải BaseActivity trong khai báo lớp của bạn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import cái này cho items trong LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
// Đã xóa các import không cần thiết:
// import androidx.activity.enableEdgeToEdge // Chưa được sử dụng
// import androidx.compose.ui.focus.FocusRequester.Companion.createRefs // createRefs được cung cấp bởi ConstraintLayoutScope
// import androidx.core.view.ViewCompat // Chưa được sử dụng
// import androidx.core.view.WindowInsetsCompat // Chưa được sử dụng
// import com.example.projectandroid.Activity.BaseActivity // Nếu CartActivity kế thừa AppCompatActivity, thì cái này không cần thiết
import com.example.projectandroid.R
import com.uilover.project2142.Helper.ManagmentCart
// Đã xóa các import sai:
// import kotlinx.coroutines.NonDisposableHandle.parent
// import kotlinx.coroutines.flow.internal.NoOpContinuation.context
// import kotlin.coroutines.jvm.internal.CompletedContinuation.context


// Kiểm tra xem CartActivity nên kế thừa BaseActivity hay AppCompatActivity.
// Dựa trên ảnh ban đầu, nó là AppCompatActivity, nhưng code của bạn lại nói BaseActivity().
// Tôi sẽ giữ AppCompatActivity như trong ảnh đầu tiên để nhất quán, giả sử BaseActivity có thể là nội bộ hoặc chưa được thiết lập đúng.
class CartActivity : AppCompatActivity() { // Đã thay đổi từ BaseActivity thành AppCompatActivity theo ảnh ban đầu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen (ManagmentCart (this), // Đã sửa lỗi truyền context
                onBackClick = {finish()})
        }
    }
}

@Composable
fun CartScreen(managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
               onBackClick: ()->Unit
) {
    val cartItem = remember { mutableStateOf(managmentCart.getListCart())} // Đã sửa lỗi gọi getListCart()
    val tax = remember { mutableStateOf(0.0) } // Đã sửa lỗi khởi tạo mutableStateOf
    calculatorCart(managmentCart, tax)

    LazyColumn (modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top=36.dp)) {
                val (backBtn, cartTxt)=createRefs() // createRefs có sẵn trong ConstraintLayoutScope
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt){centerTo(parent)},
                    text = "Your Cart",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Image(painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backBtn){
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
            // Đảm bảo bạn đã import `import androidx.compose.foundation.lazy.items` để cái này hoạt động
            items(cartItem.value) {item->
                CartItem(
                    cartItems = cartItem.value,
                    item = item,
                    managmentCart = managmentCart,
                    onItemChange = {
                        calculatorCart(managmentCart, tax)
                        // Nên tạo một ArrayList mới để recomposition
                        cartItem.value = ArrayList(managmentCart.getListCart())
                    }
                )
            }

            item{
                Text(
                    text = "Order Summary",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top=16.dp)
                )
            }

            item {
                CartSummary(
                    itemTotal = managmentCart.getTotalFee(),
                    tax = tax.value,
                    delivery = 10.0
                )
            }

            item{
                Text(
                    text = "Information",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top=16.dp)
                )
            }

            item {
                DeliveryInfoBox()
            }
        }
    }
}

fun calculatorCart(managmentCart: ManagmentCart, tax: MutableState<Double>){
    val percentTax=0.02
    tax.value=Math.round((managmentCart.getTotalFee()*percentTax)*100)/100.0
}