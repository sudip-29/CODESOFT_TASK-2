package com.devsudip.quoteoftheday.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteQuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favoriteQuote: FavoriteQuote)

    @Delete
    suspend fun removeFavorite(favoriteQuote: FavoriteQuote)

    @Query("SELECT * FROM favorite_quotes")
    suspend fun getAllFavorites(): List<FavoriteQuote>

    @Query("SELECT EXISTS(SELECT * FROM favorite_quotes WHERE text = :text)")
    suspend fun isFavorite(text: String): Boolean
}
