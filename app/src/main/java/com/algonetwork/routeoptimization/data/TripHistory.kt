package com.algonetwork.routeoptimization.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TripHistory (
    val date: String,
    val status: String,
    val from: String,
    val destination: String,
    val vehicle: Int
) : Parcelable