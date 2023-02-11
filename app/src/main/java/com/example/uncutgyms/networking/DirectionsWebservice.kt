package com.example.uncutgyms.networking

import com.example.uncutgyms.models.RoutesList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface DirectionsWebservice {

    @GET
    suspend fun getDirections(
        @Url url: String,
        @QueryMap queryMap: Map<String, String>
    ): Response<RoutesList>
}