package com.example.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.Repository.MainRepository
import com.example.projectandroid.Domain.BannerModel
import com.example.projectandroid.Domain.CategoryModel

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }
}