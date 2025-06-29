package com.example.projectandroid.Helper

import android.content.Context
import android.widget.Toast
import com.example.projectandroid.Domain.FoodModel

class ManagmentFavorite(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun addFavorite(item: FoodModel) {
        val listFavorite = getListFavorite()
        val existAlready = listFavorite.any { it.Title == item.Title }
        if (!existAlready) {
            listFavorite.add(item)
            tinyDB.putListObject("FavoriteList", listFavorite)
            Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Item already in Favorites", Toast.LENGTH_SHORT).show()
        }
    }

    fun getListFavorite(): ArrayList<FoodModel> {
        return tinyDB.getListObject("FavoriteList") ?: arrayListOf()
    }

    fun clearFavorites() {
        tinyDB.remove("FavoriteList")
    }

    fun removeFavorite(item: FoodModel) {
        val listFavorite = getListFavorite()
        val removed = listFavorite.removeIf { it.Title == item.Title }
        if (removed) {
            tinyDB.putListObject("FavoriteList", listFavorite)
            Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Item not found in Favorites", Toast.LENGTH_SHORT).show()
        }
    }
}
