package com.devsudip.quoteoftheday.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_quotes")
data class FavoriteQuote(
    @PrimaryKey val text: String,
    val author: String
)