package com.devsudip.quoteoftheday

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("quote") val text: String,
    @SerializedName("author") val author: String
)