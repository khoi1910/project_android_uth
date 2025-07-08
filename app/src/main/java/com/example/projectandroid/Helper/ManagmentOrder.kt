package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.OrderModel

class ManagmentOrder(val context: Context) {
    private val tinyDB = TinyDB(context)

    fun saveOrder(order: OrderModel) {
        val orders = getOrders().toMutableList()
        // Thêm hoặc cập nhật đơn hàng
        val index = orders.indexOfFirst { it.orderId == order.orderId }
        if (index >= 0) {
            orders[index] = order
        } else {
            orders.add(order)
        }
        // Sử dụng putObject để lưu danh sách OrderModel
        tinyDB.putObject("OrderList", ArrayList(orders))
        Toast.makeText(context, "Order saved locally", Toast.LENGTH_SHORT).show()
    }

    fun getOrders(): List<OrderModel> {
        return try {
            // Sử dụng getObject để lấy danh sách OrderModel
            val orders = tinyDB.getObject("OrderList", Array<OrderModel>::class.java)
            orders?.toList() ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }

    fun clearOrders() {
        tinyDB.remove("OrderList")
        Toast.makeText(context, "Local orders cleared", Toast.LENGTH_SHORT).show()
    }
}