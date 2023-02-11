package com.example.uncutgyms.models

import com.google.android.gms.maps.model.Polyline
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Route(
    @Json(name = "legs")
    val legs: List<DirectionLeg>?,

    @Json(name = "overview_polyline")
    val overviewPolyline: OverviewPolyline?
)
