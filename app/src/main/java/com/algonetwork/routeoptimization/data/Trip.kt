package com.algonetwork.routeoptimization.data

data class Trip(
    val routeDataList: List<RoutePoint>,
    val inputLocations: List<String>
)