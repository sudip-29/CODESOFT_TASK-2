package com.devsudip.quoteoftheday

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devsudip.quoteoftheday.database.AppDatabase
import com.devsudip.quoteoftheday.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val favoriteQuoteDao by lazy { db.favoriteQuoteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val favoriteQuotes = favoriteQuoteDao.getAllFavorites()
            binding.rvFavoriteQuotes.layoutManager = LinearLayoutManager(this@FavoritesActivity)
            binding.rvFavoriteQuotes.adapter = FavoriteQuotesAdapter(favoriteQuotes)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
