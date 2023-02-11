package com.example.uncutgyms.models

import android.os.Parcelable
import com.example.uncutgyms.ui.main.home.GymViewItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class YelpBusiness(
    @Json(name = "id")
    val id: String,

    @Json(name = "alias")
    val alias: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "image_url")
    val imageUrl: String,

    @Json(name = "is_closed")
    val isClosed: Boolean,

    @Json(name = "url")
    val url: String,

    @Json(name = "review_count")
    val reviewCount: Int,

    @Json(name = "rating")
    val rating: Double,

    @Json(name = "coordinates")
    val coordinates: LatLong,

    @Json(name = "price")
    val price: String?,

    @Json(name = "phone")
    val phone: String,

    @Json(name = "display_phone")
    val displayPhone: String,

    @Json(name = "distance")
    val distance: Float?
) : Parcelable

fun YelpBusiness.toViewItem(): GymViewItem {
    return GymViewItem.GymListItem(
        id = this.id,
        business = this
    )
}
