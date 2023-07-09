package com.wind.tryon.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TryOnItemDao {
    @Query("SELECT * from try_on_list order by itemId desc")
    fun getAll(): LiveData<List<TryOnItem>>

    @Query("SELECT * from try_on_list where itemId = :id")
    fun getById(id: Int) : TryOnItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item:TryOnItem)

    @Query("DELETE FROM try_on_list where itemId = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM try_on_list")
    suspend fun deleteAll()

}