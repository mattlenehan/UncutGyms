package com.example.uncutgyms.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class LatLong(
    @Json(name = "latitude")
    val latitude: Double?,

    @Json(name = "longitude")
    val longitude: Double?,
) : Parcelable