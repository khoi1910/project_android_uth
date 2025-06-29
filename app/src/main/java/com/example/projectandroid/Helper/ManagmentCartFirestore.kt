package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ManagmentCartFirestore(val context: Context, val userId: String) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }
    private val cartCollection = firestore.collection("users").document(userId).collection("cart")

    suspend fun insertItem(item: FoodModel) {
        try {
            val docRef = cartCollection.document(item.Id.toString())
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existingItem = snapshot.toObject(FoodModel::class.java)
                existingItem?.let {
                    it.numberInCart = item.numberInCart
                    docRef.set(it, SetOptions.merge()).await()
                }
            } else {
                docRef.set(item).await()
            }
            Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error adding to cart: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getListCart(): List<FoodModel> {
        return try {
            val snapshot = cartCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FoodModel::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading cart: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun removeItem(itemId: String) {
        try {
            cartCollection.document(itemId).delete().await()
            Toast.makeText(context, "Removed from your Cart", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error removing from cart: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun clearCart() {
        try {
            val snapshot = cartCollection.get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Toast.makeText(context, "Cart cleared", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error clearing cart: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getTotalFee(): Double {
        return try {
            val items = getListCart()
            items.sumOf { it.Price * it.numberInCart }
        } catch (e: Exception) {
            0.0
        }
    }

    // Các phương thức khác như cập nhật số lượng, lưu đơn hàng đã thanh toán có thể được thêm tương tự
}