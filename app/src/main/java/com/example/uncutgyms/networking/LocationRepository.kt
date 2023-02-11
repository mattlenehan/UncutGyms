package com.example.uncutgyms.networking

import com.example.uncutgyms.models.RoutesList
import com.example.uncutgyms.models.SearchBusinessesResponse
import com.example.uncutgyms.ui.main.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {

    val directionsFlow: StateFlow<ApiResult<RoutesList>?>

    suspend fun fetchLocation(): Flow<ApiResult<Triple<Double, Double, Double>>?>
    suspend fun getDirections(
        map: Map<String, String>
    ): Flow<ApiResult<RoutesList>?>
}
