package com.pepdeal.infotech
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.Permission
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LocationHelper(
    private val permissionsController: PermissionsController,
    private val locationTracker: LocationTracker,
    private val httpClient: HttpClient
) {
    // 1️⃣ **Request Location Permission**
    private suspend fun requestLocationPermission(): Boolean {
        val isGranted = permissionsController.isPermissionGranted(Permission.LOCATION)
        if (isGranted) return true

        return try {
            permissionsController.providePermission(Permission.LOCATION)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 2️⃣ **Fetch Current Location (Lat, Lng)**
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        if (!requestLocationPermission()) return null

        return try {
            val location = locationTracker.getLocationsFlow().first()
            Pair(location.latitude, location.longitude)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3️⃣ **Convert (Lat, Lng) to Address**
    suspend fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val response: HttpResponse = httpClient.get("https://nominatim.openstreetmap.org/reverse") {
                url {
                    parameters.append("format", "json")
                    parameters.append("lat", latitude.toString())
                    parameters.append("lon", longitude.toString())
                }
            }

            val responseBody: String = response.body()
            val json = Json.parseToJsonElement(responseBody).jsonObject
            json["display_name"]?.jsonPrimitive?.content ?: "Unknown location"
        } catch (e: Exception) {
            e.printStackTrace()
            "Unable to get location name"
        }
    }
}
