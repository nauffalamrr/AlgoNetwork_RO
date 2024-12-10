package com.algonetwork.routeoptimization.data

data class LocationsRequest(
    val locations: List<LocationCoordinates>
)

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double
)