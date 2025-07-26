package com.pepdeal.infotech

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController,
    snackBarHostState: SnackbarHostState
) {
    val initiallyGranted = controller.isPermissionGranted(permission)

    if (!initiallyGranted) {
        try {
            // Request permission
            controller.providePermission(permission)

            // Always re-check after requesting
            val grantedNow = controller.isPermissionGranted(permission)
            if (!grantedNow) {
                val result = snackBarHostState.showSnackbar(
                    message = "Permission still not granted.",
                    actionLabel = "Open Settings",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    controller.openAppSettings()
                }
            }

        } catch (e: DeniedAlwaysException) {
//            val result = snackBarHostState.showSnackbar(
//                message = "Permission permanently denied.",
//                actionLabel = "Open Settings",
//                duration = SnackbarDuration.Short
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                controller.openAppSettings()
//            }
            println("${e.permission.name} is Denied Permanently")

        } catch (e: DeniedException) {
            val result = snackBarHostState.showSnackbar(
                message = "Permission denied.",
                actionLabel = "Open Settings",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }

        } catch (e: RequestCanceledException) {
            snackBarHostState.showSnackbar(
                message = "Permission request canceled."
            )

        } catch (e: Exception) {
            snackBarHostState.showSnackbar(
                message = e.message ?: "Unknown error occurred."
            )
            e.printStackTrace()
        }
    }
}


