package com.wind.tryon.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TryOnItemViewModel(appObj: Application):AndroidViewModel(appObj) {
    private val repository:TryOnItemRepository = TryOnItemRepository(appObj)

    fun fetchAll():LiveData<List<TryOnItem>>{
        return repository.fetchAll
    }

    fun insert(item:TryOnItem){
        viewModelScope.launch {
            repository.insert(item)
        }
    }

    fun deleteById(id:Int){
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
           repository.deleteAll()
        }
    }
}