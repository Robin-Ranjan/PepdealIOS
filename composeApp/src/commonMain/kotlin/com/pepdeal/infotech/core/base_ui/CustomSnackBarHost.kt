package com.pepdeal.infotech.core.base_ui

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun CustomSnackBarHost(
    hostState: SnackbarHostState,
    currentMessage: SnackBarMessage?
) {
    SnackbarHost(hostState) { data ->
        when (currentMessage) {
            is SnackBarMessage.Error -> {
                AnimatedErrorSnackbar(data)
            }

            is SnackBarMessage.Success -> {
                AnimatedSuccessfullSnackbar(data)
            }

            else -> Unit
        }
    }
}
sealed class SnackBarMessage(val message: String) {
    class Error(message: String) : SnackBarMessage(message)
    class Success(message: String) : SnackBarMessage(message)
}