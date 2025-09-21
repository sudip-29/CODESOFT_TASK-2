package com.devsudip.quoteoftheday.network

import com.devsudip.quoteoftheday.Quote
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface QuoteApiService {
    @GET("v1/quotes")
    suspend fun getQuote(@Header("X-Api-Key") apiKey: String): Response<List<Quote>>
}