package com.example.uncutgyms.networking

import com.example.uncutgyms.models.SearchBusinessesResponse
import com.example.uncutgyms.ui.main.util.ApiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit

class GymsRepositoryImpl(
    private val gymsWebservice: GymsWebservice,
    retrofit: Retrofit,
) : BaseRepository(retrofit), GymsRepository {

    private val _gymsFlow: MutableStateFlow<ApiResult<SearchBusinessesResponse>?> =
        MutableStateFlow(null)
    override val gymsFlow: StateFlow<ApiResult<SearchBusinessesResponse>?> = _gymsFlow

    override suspend fun getGyms(
        latitude: Number,
        longitude: Number
    ): Flow<ApiResult<SearchBusinessesResponse>?> {
        return flow {
            emit(ApiResult.loading())
            val result = getResponse(
                request = {
                    gymsWebservice.searchBusinesses(
                        latitude = latitude,
                        longitude = longitude,
                        radius = 1000,
                        categories = listOf("fitness"),
                        sortBy = SortBy.DISTANCE.getKey(),
                    )
                },
                defaultErrorMessage = "Error fetching businesses"
            )

            _gymsFlow.value = ApiResult(
                result.status,
                result.data,
                result.error,
                result.message ?: result.error?.statusMessage ?: "Unable to fetch businesses"
            )
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}