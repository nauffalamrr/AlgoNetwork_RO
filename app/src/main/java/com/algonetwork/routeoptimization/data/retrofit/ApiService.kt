package com.algonetwork.routeoptimization.data.retrofit

import com.algonetwork.routeoptimization.data.AddLocationsResponse
import com.algonetwork.routeoptimization.data.GetLocationsResponse
import com.algonetwork.routeoptimization.data.LocationsRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("add_locations")
    fun addLocations(@Body body: LocationsRequest): Call<AddLocationsResponse>

    @GET("get_locations")
    fun getLocations(@Query("limit") limit: Int = 1): Call<GetLocationsResponse>
}