package com.example.uncutgyms.networking

import com.example.uncutgyms.models.SearchBusinessesResponse
import com.example.uncutgyms.ui.main.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GymsRepository {

    val gymsFlow: StateFlow<ApiResult<SearchBusinessesResponse>?>

    suspend fun getGyms(
        latitude: Number,
        longitude: Number
    ): Flow<ApiResult<SearchBusinessesResponse>?>
}