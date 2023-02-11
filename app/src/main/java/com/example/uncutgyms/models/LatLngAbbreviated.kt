package com.example.uncutgyms.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class LatLngAbbreviated(
    @Json(name = "lat")
    val lat: Double?,

    @Json(name = "lng")
    val lng: Double?,
) : Parcelable