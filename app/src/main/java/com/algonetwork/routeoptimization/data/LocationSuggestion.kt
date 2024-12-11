package com.algonetwork.routeoptimization.data

import com.google.gson.annotations.SerializedName

data class LocationSuggestion(
    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("lat")
    val latitude: Double,

    @SerializedName("lon")
    val longitude: Double
)
