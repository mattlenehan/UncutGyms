package com.example.uncutgyms.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uncutgyms.models.LatLong
import com.example.uncutgyms.models.RoutesList
import com.example.uncutgyms.models.SearchBusinessesResponse
import com.example.uncutgyms.models.toViewItem
import com.example.uncutgyms.networking.GymsRepository
import com.example.uncutgyms.networking.LocationRepository
import com.example.uncutgyms.ui.main.home.GymViewItem
import com.example.uncutgyms.ui.main.home.GymsTabOption
import com.example.uncutgyms.ui.main.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val gymsRepository: GymsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _gyms = MutableLiveData<ApiResult<List<GymViewItem>?>>()
    val gyms: LiveData<ApiResult<List<GymViewItem>?>> = _gyms

    private val _tabFlow = MutableStateFlow(GymsTabOption.MAP)
    val tabFlow: StateFlow<GymsTabOption> = _tabFlow

    private val _locationStatus = MutableLiveData<ApiResult<LatLong>>()
    val locationStatus: LiveData<ApiResult<LatLong>> = _locationStatus

    private val _directions = MutableLiveData<ApiResult<RoutesList>>()
    val directions: LiveData<ApiResult<RoutesList>> = _directions

    init {
        viewModelScope.launch {
            gymsRepository.gymsFlow.collect { result ->
                when (result?.status) {
                    ApiResult.Status.SUCCESS -> {
                        updateUi(result.data)
                    }
                    ApiResult.Status.ERROR -> {
                        _gyms.value = ApiResult.error(
                            result.message ?: "Unable to fetch gyms",
                            result.error
                        )
                    }
                    ApiResult.Status.LOADING -> {
                        _gyms.value = ApiResult.loading()
                    }
                    null -> {}
                }
            }
        }
    }

    fun getGyms(
        latitude: Number,
        longitude: Number
    ) {
        viewModelScope.launch {
            gymsRepository.getGyms(
                latitude = latitude,
                longitude = longitude
            ).collect {
                it?.data?.let { response ->
                    updateUi(response)
                }
            }
        }
    }

    fun findLocation() {
        viewModelScope.launch {
            _locationStatus.value = ApiResult.loading()
            locationRepository.fetchLocation().collect { apiResult ->
                val data = apiResult?.data
                if (apiResult?.status == ApiResult.Status.SUCCESS && data != null) {
                    _locationStatus.value = ApiResult.success(
                        LatLong(
                            latitude = data.first,
                            longitude = data.second
                        )
                    )
                } else if (apiResult?.status == ApiResult.Status.ERROR) {
                    _locationStatus.value = ApiResult.error("unable to fetch location", null)
                }
            }
        }
    }

    fun getDirections(map: Map<String, String>) {
        viewModelScope.launch {
            _directions.value = ApiResult.loading()
            locationRepository.getDirections(map).collect { apiResult ->
                val data = apiResult?.data
                if (apiResult?.status == ApiResult.Status.SUCCESS && data != null) {
                    _directions.value = ApiResult.success(apiResult.data)
                } else if (apiResult?.status == ApiResult.Status.ERROR) {
                    _directions.value = ApiResult.error("unable to fetch directions", null)
                }
            }
        }
    }

    private fun updateUi(response: SearchBusinessesResponse?) {
        val gyms = response?.businesses ?: emptyList()
        val viewItems = mutableListOf<GymViewItem>()
        viewItems.addAll(gyms.map {
            it.toViewItem()
        })
        _gyms.value = ApiResult.success(viewItems)
    }

    internal fun onTabSelected(tab: GymsTabOption) {
        viewModelScope.launch {
            _tabFlow.emit(tab)
        }
    }
}