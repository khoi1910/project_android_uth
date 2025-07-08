package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagmentCartSync(val context: Context, val userId: String) {

    private val managmentCart = ManagmentCart(context)
    private val managmentCartFirestore = ManagmentCartFirestore(context, userId)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun syncFromFirestoreToLocal(onComplete: (() -> Unit)? = null) {
        coroutineScope.launch {
            val firestoreCart = managmentCartFirestore.getListCart()
            managmentCart.clearCart()
            firestoreCart.forEach {
                managmentCart.insertItem(it)
            }
            onComplete?.invoke()
        }
    }

    fun syncFromLocalToFirestore(onComplete: (() -> Unit)? = null) {
        coroutineScope.launch {
            val localCart = managmentCart.getListCart()
            // Clear Firestore cart first
            managmentCartFirestore.clearCart()
            // Insert all local items to Firestore
            localCart.forEach {
                managmentCartFirestore.insertItem(it)
            }
            onComplete?.invoke()
        }
    }

    fun addItem(item: FoodModel) {
        managmentCart.insertItem(item)
        coroutineScope.launch {
            managmentCartFirestore.insertItem(item)
        }
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun clearCart() {
        managmentCart.clearCart()
        coroutineScope.launch {
            managmentCartFirestore.clearCart()
        }
        Toast.makeText(context, "Cart cleared", Toast.LENGTH_SHORT).show()
    }
}
