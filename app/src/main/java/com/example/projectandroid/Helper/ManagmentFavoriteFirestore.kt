package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await

class ManagmentFavoriteFirestore(val context: Context, val userId: String) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }
    private val favoriteCollection = firestore.collection("users").document(userId).collection("favorites")

    suspend fun addFavorite(item: FoodModel) {
        try {
            val docRef = favoriteCollection.document(item.Id.toString())
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                docRef.set(item).await()
                Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Item already in Favorites", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error adding to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getListFavorite(): List<FoodModel> {
        return try {
            val snapshot = favoriteCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FoodModel::class.java) }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }

    suspend fun removeFavorite(itemId: String) {
        try {
            favoriteCollection.document(itemId).delete().await()
            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error removing from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun clearFavorites() {
        try {
            val snapshot = favoriteCollection.get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Toast.makeText(context, "Favorites cleared", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error clearing favorites: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}