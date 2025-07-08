package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.OrderModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ManagmentOrderFirestore(val context: Context, val userId: String) {

    private val firestore = FirebaseFirestore.getInstance()
    private val orderCollection = firestore.collection("users").document(userId).collection("orders")

    suspend fun savePaidOrder(order: OrderModel) {
        try {
            val docRef = orderCollection.document(order.orderId)
            docRef.set(order).await()
            Toast.makeText(context, "Order saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving order: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getPaidOrders(): List<OrderModel> {
        return try {
            val snapshot = orderCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(OrderModel::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun clearPaidOrders() {
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

    suspend fun hasPaidOrders(): Boolean {
        return try {
            val snapshot = orderCollection.get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}