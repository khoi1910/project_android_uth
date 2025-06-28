package com.example.projectandroid.Activity.Dashboard // Đảm bảo tên gói đúng với thư mục của bạn

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projectandroid.Activity.Cart.CartActivity
import com.example.projectandroid.R


@Composable
@Preview
fun MyBottomBar(){
    val bottomMenuItemsList = prepareBottomMenu()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Home") } // <-- Đã sửa lỗi cú pháp `value:`

    BottomAppBar (
        backgroundColor = colorResource(R.color.grey)
    ) {
        bottomMenuItemsList.forEach{ bottomMenuItem ->
            BottomNavigationItem(
                selected = (selectedItem == bottomMenuItem.label),
                onClick = {
                    selectedItem = bottomMenuItem.label
                    if(bottomMenuItem.label=="Cart"){
                        context.startActivity(Intent(context, CartActivity::class.java))
                    } else if (bottomMenuItem.label == "Order") {
                        context.startActivity(Intent(context, com.example.projectandroid.Activity.OrderStatus.OrderStatusActivity::class.java))
                    } else {
                        Toast.makeText(context, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                    }
                },
                icon = {
                    Icon(painter = bottomMenuItem.icon,
                        contentDescription = bottomMenuItem.label, // Nên có contentDescription cho khả năng tiếp cận
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(20.dp))
                }
            )
        }
    }
}

data class BottomMenuItem(
    val label: String,
    val icon: Painter
)

@Composable
fun prepareBottomMenu(): List<BottomMenuItem>{
    val bottomMenuItemList = arrayListOf<BottomMenuItem>()

    bottomMenuItemList.add(BottomMenuItem(label = "Home",icon= painterResource(R.drawable.btn_1)))
    bottomMenuItemList.add(BottomMenuItem(label = "Cart",icon= painterResource(R.drawable.btn_2)))
    bottomMenuItemList.add(BottomMenuItem(label = "Favorite",icon= painterResource(R.drawable.btn_3)))
    bottomMenuItemList.add(BottomMenuItem(label = "Order",icon= painterResource(R.drawable.btn_4)))
    bottomMenuItemList.add(BottomMenuItem(label = "Profile",icon= painterResource(R.drawable.btn_5)))

    return bottomMenuItemList
}