package com.example.uncutgyms.ui.main.util

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    val statusCode: Int = 0,
    val statusMessage: String? = null
)
