package com.devsudip.quoteoftheday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devsudip.quoteoftheday.database.FavoriteQuote

class FavoriteQuotesAdapter(private val quotes: List<FavoriteQuote>) :
    RecyclerView.Adapter<FavoriteQuotesAdapter.QuoteViewHolder>() {

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteText: TextView = itemView.findViewById(R.id.tvQuoteText)
        val quoteAuthor: TextView = itemView.findViewById(R.id.tvQuoteAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.quoteText.text = quote.text
        holder.quoteAuthor.text = "- ${quote.author}"
    }

    override fun getItemCount() = quotes.size
}
