package com.devsudip.quoteoftheday

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.devsudip.quoteoftheday.database.AppDatabase
import com.devsudip.quoteoftheday.database.FavoriteQuote
import com.devsudip.quoteoftheday.databinding.ActivityMainBinding
import com.devsudip.quoteoftheday.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentQuote: Quote? = null

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val favoriteQuoteDao by lazy { db.favoriteQuoteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchQuoteOfTheDay()
        setupClickListeners()
    }

    private fun fetchQuoteOfTheDay() {
        val prefs = getSharedPreferences("QuoteApp", MODE_PRIVATE)
        val lastUpdate = prefs.getLong("last_update_day", 0)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (lastUpdate.toInt() != today) {
            getNewQuoteFromApi()
        } else {
            val text = prefs.getString("daily_quote_text", null)
            val author = prefs.getString("daily_quote_author", null)
            if (!text.isNullOrEmpty() && author != null) {
                currentQuote = Quote(text, author)
                displayQuote()
            } else {
                getNewQuoteFromApi() // Fallback if saved quote is invalid
            }
        }
    }

    private fun getNewQuoteFromApi() {
        val apiKey = BuildConfig.API_NINJAS_KEY
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please add your API Key in local.properties!", Toast.LENGTH_LONG).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getQuote(apiKey)
                if (response.isSuccessful && response.body() != null) {
                    val quotes = response.body()!!
                    if (quotes.isNotEmpty()) {
                        currentQuote = quotes[0]
                        saveQuoteForToday(currentQuote!!)
                        displayQuote()
                    }
                } else {
                    handleApiError("Failed to fetch quote. Response code: ${response.code()}")
                }
            } catch (e: Exception) {
                handleApiError("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun saveQuoteForToday(quote: Quote) {
        val prefs = getSharedPreferences("QuoteApp", MODE_PRIVATE)
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        prefs.edit().apply {
            putLong("last_update_day", today.toLong())
            putString("daily_quote_text", quote.text)
            putString("daily_quote_author", quote.author)
            apply()
        }
    }

    private fun displayQuote() {
        currentQuote?.let {
            binding.tvQuoteText.text = it.text
            binding.tvQuoteAuthor.text = "- ${it.author}"
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        lifecycleScope.launch {
            val isFav = currentQuote?.let { favoriteQuoteDao.isFavorite(it.text) } ?: false
            if (isFav) {
                binding.fabFavorite.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_favorite_filled))
            } else {
                binding.fabFavorite.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_favorite_border))
            }
        }
    }

    private fun handleApiError(message: String) {
        Log.e("API_ERROR", message)
        Toast.makeText(this@MainActivity, "Failed to load a new quote.", Toast.LENGTH_SHORT).show()
    }

    // --- NEW SCREENSHOT AND SHARING LOGIC ---

    private fun takeScreenshot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmap(bitmap: Bitmap): Uri? {
        val imagePath = File(cacheDir, "images")
        imagePath.mkdirs()
        val file = File(imagePath, "quote_screenshot.png")
        return try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Quote"))
    }


    private fun setupClickListeners() {
        // --- UPDATED SHARE BUTTON LOGIC ---
        binding.fabShare.setOnClickListener {
            // Hide buttons before taking the screenshot
            binding.fabFavorite.visibility = View.INVISIBLE
            binding.fabShare.visibility = View.INVISIBLE
            binding.btnViewFavorites.visibility = View.INVISIBLE

            val screenshotBitmap = takeScreenshot(binding.root)
            val uri = saveBitmap(screenshotBitmap)

            // Show buttons again after screenshot is taken
            binding.fabFavorite.visibility = View.VISIBLE
            binding.fabShare.visibility = View.VISIBLE
            binding.btnViewFavorites.visibility = View.VISIBLE

            if (uri != null) {
                shareImageUri(uri)
            } else {
                Toast.makeText(this, "Failed to share quote image.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fabFavorite.setOnClickListener {
            lifecycleScope.launch {
                currentQuote?.let { quote ->
                    val isFav = favoriteQuoteDao.isFavorite(quote.text)
                    if (isFav) {
                        favoriteQuoteDao.removeFavorite(FavoriteQuote(quote.text, quote.author))
                    } else {
                        favoriteQuoteDao.addFavorite(FavoriteQuote(quote.text, quote.author))
                    }
                    updateFavoriteIcon()
                }
            }
        }

        binding.btnViewFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }
    }
}