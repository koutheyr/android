package com.kou.uniclub.Model

import com.google.gson.annotations.SerializedName

data class FeedResponse(
    @SerializedName("data")
    val `data`: List<Event>,
    @SerializedName("success")
    val success: Boolean
)