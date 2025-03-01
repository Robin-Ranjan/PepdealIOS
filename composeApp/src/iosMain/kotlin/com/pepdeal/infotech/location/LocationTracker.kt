package com.pepdeal.infotech.location

import dev.icerock.moko.geo.Location
import kotlinx.coroutines.flow.Flow

// A simple data class for location coordinates.
data class Location(val latitude: Double, val longitude: Double)

interface LocationTracker {
    fun getLocationFlow(): Flow<Location?>
}