package com.pepdeal.infotech.registration

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pepdealios.composeapp.generated.resources.Res
import utils.KottieConstants


@OptIn(ExperimentalResourceApi::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    sessionInfo: String,
    coroutineScope: CoroutineScope,
    showSnackBar: (String) -> Unit,
    sessionInfoChange: (String) -> Unit,
    isOtpAuthenticated: () -> Unit
) {

    var otpCode by remember { mutableStateOf("") }
    var otpAnimation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val otpComposition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(otpAnimation)
    )

    val otpAnimationState by animateKottieCompositionAsState(
        composition = otpComposition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        try {
            otpAnimation = Res.readBytes("files/otp_anim.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).pointerInput(Unit) {
            detectTapGestures(onTap = {
                keyboardController?.hide()
            })
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Lottie Animation
        KottieAnimation(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = 50.dp),
            composition = otpComposition,
            progress = { otpAnimationState.progress }
        )
        Text(
            text = "Enter OTP Sent to $phoneNumber",
            fontSize = 12.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it },
            label = { Text(text = "Enter OTP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    keyboardController?.hide()
                    isLoading = true
                    coroutineScope.launch {
                        val idToken = AuthRepository.verifyOtp(otpCode, sessionInfo)
                        if (idToken != null) {
                            showSnackBar("User authenticated!")
                            isOtpAuthenticated()
                        } else {
                            showSnackBar("OTP verification failed!")
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Verify OTP")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                coroutineScope.launch {
                    val result = AuthRepository.sendOtp(phoneNumber)
                    if (result != null) {
                        sessionInfoChange(result)
                        showSnackBar("OTP Resent Successfully!")
                    } else {
                        showSnackBar("Failed to resend OTP.")
                    }
                }
            }
        ) {
            Text(text = "Resend OTP")
        }
    }
}