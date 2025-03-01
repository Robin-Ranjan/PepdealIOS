package com.pepdeal.infotech

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel(
    private val permissionsController: PermissionsController,
    private val locationTracker: LocationTracker,
    private val httpClient: HttpClient
) : ViewModel() {

    private val locationHelper = LocationHelper(permissionsController, locationTracker, httpClient)

    private val _locationName = MutableStateFlow("Fetching location...")
    val locationName: StateFlow<String> = _locationName

    fun fetchLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                val (latitude, longitude) = location
                _locationName.value = locationHelper.getLocationName(latitude, longitude)
            } else {
                _locationName.value = "Location not available"
            }
        }
    }
}
