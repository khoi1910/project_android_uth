package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagmentFavoriteSync(val context: Context, val userId: String) {

    private val managmentFavorite = ManagmentFavorite(context)
    private val managmentFavoriteFirestore = ManagmentFavoriteFirestore(context, userId)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun syncFromFirestoreToLocal(onComplete: (() -> Unit)? = null) {
        coroutineScope.launch {
            val firestoreFavorites = managmentFavoriteFirestore.getListFavorite()
            managmentFavorite.clearFavorites()
            firestoreFavorites.forEach {
                managmentFavorite.addFavorite(it)
            }
            onComplete?.invoke()
        }
    }

    fun syncFromLocalToFirestore(onComplete: (() -> Unit)? = null) {
        coroutineScope.launch {
            val localFavorites = managmentFavorite.getListFavorite()
            // Clear Firestore favorites first
            managmentFavoriteFirestore.clearFavorites()
            // Insert all local favorites to Firestore
            localFavorites.forEach {
                managmentFavoriteFirestore.addFavorite(it)
            }
            onComplete?.invoke()
        }
    }

    fun addFavorite(item: FoodModel) {
        managmentFavorite.addFavorite(item)
        coroutineScope.launch {
            managmentFavoriteFirestore.addFavorite(item)
        }
        Toast.makeText(context, "Added to your Favorites", Toast.LENGTH_SHORT).show()
    }

    fun clearFavorite() {
        managmentFavorite.clearFavorites()
        coroutineScope.launch {
            managmentFavoriteFirestore.clearFavorites()
        }
        Toast.makeText(context, "Favorites cleared", Toast.LENGTH_SHORT).show()
    }
}
