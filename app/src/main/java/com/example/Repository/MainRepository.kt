package com.example.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projectandroid.Domain.BannerModel
import com.example.projectandroid.Domain.CategoryModel
import com.example.projectandroid.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MainRepository {

    private val firebaseDatabase =
        FirebaseDatabase.getInstance("https://projectandroid-87f0c-default-rtdb.asia-southeast1.firebasedatabase.app")

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Error loading categories: ${error.message}")
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banners")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    item?.let { list.add(it) }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Error loading banners: ${error.message}")
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods")
        val query: Query = ref.orderByChild("CategoryId").equalTo(id)

        Log.d("MainRepository", "Loading items for category ID: $id")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<FoodModel>()
                Log.d("MainRepository", "DataSnapshot exists: ${snapshot.exists()}")
                Log.d("MainRepository", "DataSnapshot children count: ${snapshot.childrenCount}")

                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(FoodModel::class.java)
                    if (list != null) {
                        Log.d("MainRepository", "Found item: ${list.Title} with CategoryId: ${list.CategoryId}")
                        lists.add(list)
                    }
                }
                Log.d("MainRepository", "Total items found: ${lists.size}")
                listData.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Error loading filtered data: ${error.message}")
                listData.value = mutableListOf()
            }
        })
        return listData
    }
}