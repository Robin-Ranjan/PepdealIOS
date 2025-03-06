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
    val granted = controller.isPermissionGranted(permission)
    if (!granted) {
        try {
            controller.providePermission(permission)
        } catch (e: DeniedException) {

            val result = snackBarHostState.showSnackbar(
                message = "Permission denied",
                actionLabel = "Open Settings",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }
        } catch (e: DeniedAlwaysException) {
            val result = snackBarHostState.showSnackbar(
                message = "Permanently denied",
                actionLabel = "Open Settings",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }

        } catch (e: RequestCanceledException) {
            snackBarHostState.showSnackbar(
                message = "Request cancelled."
            )
        }
    }
}

