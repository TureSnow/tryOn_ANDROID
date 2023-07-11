package com.wind.tryon.database

import android.app.Application
import androidx.lifecycle.LiveData

class TryOnItemRepository(application: Application) {
    private var dao:TryOnItemDao
    init {
        val database = TryOnDataBase.getInstance(application)
        dao = database.tryOnDao()
    }
    val fetchAll : LiveData<List<TryOnItem>> =  dao.getAll()

    suspend fun insert(TryOnItem: TryOnItem) {
        dao.insert(TryOnItem)
    }

    suspend fun deleteItemById(id: Int) {
        dao.deleteItemById(id)
    }

    suspend fun deleteAll(){
        dao.deleteAll()
    }
}