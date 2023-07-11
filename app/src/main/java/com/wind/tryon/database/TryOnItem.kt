package com.wind.tryon.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "try_on_list")
data class TryOnItem(
    @PrimaryKey(autoGenerate = true)
    var itemId: Int = 0,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "cloth_uri")
    val cloth_base64: String,

    @ColumnInfo(name = "person_uri")
    val person_base64: String
)