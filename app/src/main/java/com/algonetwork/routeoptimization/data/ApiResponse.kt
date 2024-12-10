package com.algonetwork.routeoptimization.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddLocationsResponse(
    val message: String,
    val status: String
) : Parcelable

@Parcelize
data class GetLocationsResponse(
    val routes: List<List<RoutePoint>>,
    val status: String
) : Parcelable

@Parcelize
data class RoutePoint(
    val latitude: Double,
    val longitude: Double
) : Parcelable