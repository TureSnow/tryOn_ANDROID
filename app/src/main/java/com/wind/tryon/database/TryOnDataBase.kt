package com.wind.tryon.database

import android.content.Context
import androidx.room.Database;
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TryOnItem::class], version = 1)
abstract class TryOnDataBase:RoomDatabase(){
    abstract fun tryOnDao():TryOnItemDao
    companion object {

        @Volatile
        private var INSTANCE: TryOnDataBase? = null

        fun getInstance(context: Context): TryOnDataBase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TryOnDataBase::class.java,
                        "database")
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}