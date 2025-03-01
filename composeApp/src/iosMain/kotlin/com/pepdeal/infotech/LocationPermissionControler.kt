package com.pepdeal.infotech

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.product.addProduct.requestPermission
import dev.icerock.moko.geo.Location
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun locationPermissionController(
    controller: PermissionsController,
    snackBar: SnackbarHostState,
    scope: CoroutineScope
) {
    scope.launch {
        requestLocationPermission(
            controller,
            permission = Permission.LOCATION,
            snackBarHostState = snackBar
        )
    }
}

//class LocationHelper(
//    private val permissionsController: PermissionsController,
//    private val locationTracker: LocationTracker,
//    private val httpClient: HttpClient
//) {
//
//    // 1️⃣ **Request Location Permission**
//    suspend fun requestLocationPermission(): Boolean {
//        val isGranted = permissionsController.isPermissionGranted(Permission.LOCATION)
//        if (isGranted) return true
//
//        return try {
//            permissionsController.providePermission(Permission.LOCATION)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    suspend fun getCurrentLocation(): Pair<Double, Double>? {
//        val isGranted = permissionsController.isPermissionGranted(Permission.LOCATION)
//        if (!isGranted) return null
//
//        return try {
//            val location = locationTracker.getLocationsFlow().first()
//            Pair(location.latitude, location.longitude)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    suspend fun getLocationName(latitude: Double, longitude: Double): String {
//        return try {
//            val response: HttpResponse = httpClient.get("https://nominatim.openstreetmap.org/reverse") {
//                url {
//                    parameters.append("format", "json")
//                    parameters.append("lat", latitude.toString())
//                    parameters.append("lon", longitude.toString())
//                }
//            }
//
//            val responseBody: String = response.body()
//            val json = Json.parseToJsonElement(responseBody).jsonObject
//            json["display_name"]?.jsonPrimitive?.content ?: "Unknown location"
//        } catch (e: Exception) {
//            e.printStackTrace()
//            "Unable to get location name"
//        }
//    }
//}


suspend fun requestLocationPermission(
    controller: PermissionsController,
    permission: Permission,
    snackBarHostState: SnackbarHostState
): Boolean {
    return try {
        when (controller.getPermissionState(permission)) {
            PermissionState.Granted -> true
            PermissionState.Denied -> {
                controller.providePermission(permission)
                controller.isPermissionGranted(permission) // ✅ Check again after requesting
            }
            PermissionState.DeniedAlways -> {
                val result = snackBarHostState.showSnackbar(
                    message = "Permission denied. Open settings to grant it.",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    controller.openAppSettings()
                }
                false
            }
            PermissionState.NotDetermined, PermissionState.NotGranted -> {
                controller.providePermission(permission)
                controller.isPermissionGranted(permission) // ✅ Recheck if granted
            }
        }
    } catch (e: DeniedException) {
        snackBarHostState.showSnackbar("Permission Denied", duration = SnackbarDuration.Short)
        false
    } catch (e: DeniedAlwaysException) {
        val result = snackBarHostState.showSnackbar(
            message = "Permission denied. Open settings to grant it.",
            actionLabel = "Settings",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            controller.openAppSettings()
        }
        false
    } catch (e: RequestCanceledException) {
        snackBarHostState.showSnackbar("Permission request Canceled")
        false
    }
}
