package com.algonetwork.routeoptimization.data.retrofit

import com.algonetwork.routeoptimization.data.AddLocationsResponse
import com.algonetwork.routeoptimization.data.GetLocationsResponse
import com.algonetwork.routeoptimization.data.LocationsRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("add_locations")
    fun addLocations(@Body body: LocationsRequest): Call<AddLocationsResponse>

    @GET("solve_vrp")
    fun getLocations(): Call<GetLocationsResponse>
}