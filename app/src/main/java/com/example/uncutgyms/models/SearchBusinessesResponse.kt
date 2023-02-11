package com.example.uncutgyms.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchBusinessesResponse(
    @Json(name = "businesses")
    val businesses: List<YelpBusiness>
)
