package com.example.uncutgyms.networking

import com.example.uncutgyms.models.SearchBusinessesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GymsWebservice {

    @GET("/v3/businesses/search")
    suspend fun searchBusinesses(
        @Query("latitude") latitude: Number,
        @Query("longitude") longitude: Number,
        @Query("radius") radius: Int,
        @Query("categories") categories: List<String>,
        @Query("sort_by") sortBy: String,
    ): Response<SearchBusinessesResponse>
}

enum class SortBy {
    BEST_MATCH,
    RATING,
    REVIEW_COUNT,
    DISTANCE
}

fun SortBy.getKey(): String {
    return when (this) {
        SortBy.BEST_MATCH -> "best_match"
        SortBy.RATING -> "rating"
        SortBy.REVIEW_COUNT -> "review_count"
        SortBy.DISTANCE -> "distance"
    }
}