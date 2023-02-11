package com.example.uncutgyms.networking

import android.content.Context
import android.location.Location
import com.example.uncutgyms.models.RoutesList
import com.example.uncutgyms.ui.main.util.ApiResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit

class LocationRepositoryImpl(
    private val directionsWebservice: DirectionsWebservice,
    retrofit: Retrofit,
    context: Context,
) : BaseRepository(retrofit), LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _directionsFlow: MutableStateFlow<ApiResult<RoutesList>?> =
        MutableStateFlow(null)
    override val directionsFlow: StateFlow<ApiResult<RoutesList>?> = _directionsFlow

    override suspend fun fetchLocation(): Flow<ApiResult<Triple<Double, Double, Double>>?> {
        return callbackFlow {
            trySend(ApiResult.loading(null))
            try {
                // Try a fast location lookup first. This will almost always immediately return
                // a valid, recent location.
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        try {
                            // If "lastLocation" did not work, try "currentLocation". This covers
                            // some rare edge cases, like if the user hasn't opened Google Maps
                            // or given any other apps location permissions.
                            val cancellationTokenSource = CancellationTokenSource()
                            val task = fusedLocationClient.getCurrentLocation(
                                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                                cancellationTokenSource.token,
                            )
                            task.addOnSuccessListener { loc: Location? ->
                                if (loc == null) {
                                    trySend(
                                        ApiResult.error(
                                            message = "No location found", null
                                        )
                                    )
                                } else {
                                    trySend(
                                        ApiResult.success(
                                            Triple(
                                                loc.latitude,
                                                loc.longitude,
                                                loc.altitude
                                            )
                                        )
                                    )
                                }
                            }
                        } catch (exception: SecurityException) {
                            trySend(ApiResult.error(message = "No permissions", null))
                        }
                    } else {
                        trySend(
                            ApiResult.success(
                                Triple(
                                    location.latitude,
                                    location.longitude,
                                    location.altitude
                                )
                            )
                        )
                    }
                    Unit
                }
            } catch (exception: SecurityException) {
                trySend(ApiResult.error(message = "No permissions", null))
            }

            awaitClose()
        }
    }

    override suspend fun getDirections(map: Map<String, String>): Flow<ApiResult<RoutesList>?> {
        return flow {
            emit(ApiResult.loading())
            val result = getResponse(
                request = {
                    directionsWebservice.getDirections(
                        "https://maps.googleapis.com/maps/api/directions/json",
                        map
                    )
                },
                defaultErrorMessage = "Error fetching directions"
            )
            _directionsFlow.value = ApiResult(
                result.status,
                result.data,
                result.error,
                result.message ?: result.error?.statusMessage ?: "Unable to fetch directions"
            )

            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}
