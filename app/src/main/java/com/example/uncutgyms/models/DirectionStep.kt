package com.example.uncutgyms.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DirectionStep(
    @Json(name = "start_location")
    val startLocation: LatLngAbbreviated?,

    @Json(name = "end_location")
    val endLocation: LatLngAbbreviated?,
)