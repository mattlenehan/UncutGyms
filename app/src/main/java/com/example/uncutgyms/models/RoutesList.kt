package com.example.uncutgyms.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoutesList(
    @Json(name = "geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypoints>,

    @Json(name = "routes")
    val routes: List<Route>?,

    @Json(name = "status")
    val status: String
)
