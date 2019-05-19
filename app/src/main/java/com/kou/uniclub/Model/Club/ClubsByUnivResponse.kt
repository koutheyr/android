package com.kou.uniclub.Model.Club

import com.google.gson.annotations.SerializedName

data class ClubsByUnivResponse(
    @SerializedName("data")
    val clubs: ArrayList<ClubX>,
    @SerializedName("success")
    val success: Boolean
)