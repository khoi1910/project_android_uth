package com.example.projectandroid.Domain

import java.io.Serializable

data class OrderModel(
    var orderId: String = "",
    var orderDate: Long = 0L,
    var totalAmount: Double = 0.0,
    var items: List<FoodModel> = listOf(),
    var status: String = "Completed" // trạng thái đơn hàng
) : Serializable
