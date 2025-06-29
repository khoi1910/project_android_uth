package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ManagmentOrderFirestore(val context: Context, val userId: String) {

    private val firestore = FirebaseFirestore.getInstance()
    private val orderCollection = firestore.collection("users").document(userId).collection("orders")

    suspend fun savePaidOrder(paidItems: List<FoodModel>) {
        try {
            val batch = firestore.batch()
            // Xóa đơn hàng cũ trước khi lưu đơn hàng mới
            val oldOrders = orderCollection.get().await()
            for (doc in oldOrders.documents) {
                batch.delete(doc.reference)
            }
            // Thêm đơn hàng mới
            for (item in paidItems) {
                val docRef = orderCollection.document(item.Id.toString())
                batch.set(docRef, item)
            }
            batch.commit().await()
            Toast.makeText(context, "Order saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving order: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getPaidOrder(): List<FoodModel> {
        return try {
            val snapshot = orderCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FoodModel::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun clearPaidOrder() {
        try {
            val snapshot = orderCollection.get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Toast.makeText(context, "Orders cleared", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error clearing orders: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun hasPaidOrder(): Boolean {
        return try {
            val snapshot = orderCollection.get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getPaidOrderTotal(): Double {
        return try {
            val orders = getPaidOrder()
            orders.sumOf { it.Price * it.numberInCart }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getPaidOrderItemCount(): Int {
        return try {
            val orders = getPaidOrder()
            orders.sumOf { it.numberInCart }
        } catch (e: Exception) {
            0
        }
    }
}
