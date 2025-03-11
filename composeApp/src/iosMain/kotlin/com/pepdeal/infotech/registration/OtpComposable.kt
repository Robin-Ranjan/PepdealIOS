package com.pepdeal.infotech.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import com.pepdeal.infotech.util.NavigationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pepdealios.composeapp.generated.resources.Res
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import utils.KottieConstants


@OptIn(ExperimentalResourceApi::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    coroutineScope: CoroutineScope,
    showSnackBar: (String) -> Unit,
    isOtpAuthenticated: () -> Unit,
    isForgetPass:Boolean = false
) {

    var otpCode by remember { mutableStateOf("") }
    var otpAnimation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isResending by remember { mutableStateOf(false) }
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


    var remainingTime by remember { mutableStateOf(30) } // 30 sec cooldown
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(remainingTime) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime--
        }
        canResend = true
    }
    // Format seconds into MM:SS
    val formattedTime = NSString.stringWithFormat("%02d:%02d", remainingTime / 60, remainingTime % 60)

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp).pointerInput(Unit) {
            detectTapGestures(onTap = {
                keyboardController?.hide()
            })
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.fillMaxWidth()){
            IconButton(
                onClick = {
                    NavigationProvider.navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.TopStart)
                    .zIndex(1f)
            ){
                Icon(
                    imageVector =  Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Black
                )
            }
        }

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
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                        val idToken = AuthRepository.verifyOtp(phoneNumber,otpCode)
                        if (idToken) {
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

        Spacer(modifier = Modifier.height(8.dp))

        // 📌 Resend OTP using TextButton
        TextButton(
            onClick = {
                if (!isResending) {
                    isResending = true
                    coroutineScope.launch {
                        val result = AuthRepository.sendOtp("91$phoneNumber", isResend = true, isForgotPassword = isForgetPass)
                        if (result) {
                            showSnackBar("OTP Resent Successfully!")
                            remainingTime = 30 // Reset cooldown
                            canResend = false
                        } else {
                            showSnackBar("Failed to resend OTP.")
                        }
                        isResending = false
                    }
                }
            },
            enabled = canResend && !isResending
        ) {
            if (isResending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (canResend) "Resend OTP ?" else "Resend in $formattedTime sec",
                    color = if (canResend) MaterialTheme.colorScheme.primary else Color.DarkGray
                )
            }
        }
    }
}