package com.example.uncutgyms.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodedWaypoints(
    @Json(name = "geocoder_status")
    val geocoder_status: String,

    @Json(name = "place_id")
    val place_id : String,

    @Json(name = "types")
    val types : List<String>
)
