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
    val data: List<RouteData>,
    val status: String
) : Parcelable

@Parcelize
data class RouteData(
    val created_at: String,
    val id: String,
    val locations: List<LocationData>
) : Parcelable

@Parcelize
data class LocationData(
    val latitude: Double,
    val longitude: Double
) : Parcelable