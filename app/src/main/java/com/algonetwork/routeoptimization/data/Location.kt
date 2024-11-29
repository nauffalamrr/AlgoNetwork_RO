package com.algonetwork.routeoptimization.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Parcelable