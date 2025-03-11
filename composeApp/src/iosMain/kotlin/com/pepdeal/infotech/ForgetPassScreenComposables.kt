package com.pepdeal.infotech

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.registration.AuthRepository
import com.pepdeal.infotech.registration.OtpVerificationScreen
import com.pepdeal.infotech.util.NavigationProvider.navController
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import utils.KottieConstants

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgetPassScreen() {

    // variables
    var animation by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordResetScreen by remember { mutableStateOf(false) }
    var showOtpAuthScreen by remember { mutableStateOf(false) }

    // constants
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/forget_pss_anim.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    val composition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(animation)
    )

    val animationState by animateKottieCompositionAsState(
        composition = composition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    suspend fun showSnackBar(message: String) {
        snackBarHostState.showSnackbar(message)
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Text(
                            "Forget Pass",
                            fontSize = 17.ssp,
                            lineHeight = 17.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,  // Background color
                        titleContentColor = Color.Black,  // Title color
                        navigationIconContentColor = Color.Black,  // Back button color
                        actionIconContentColor = Color.Unspecified
                    )
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                if (!showPasswordResetScreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 30.dp)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    keyboardController?.hide()
                                }
                            }
                    ) {

                        // Lottie Animation
                        KottieAnimation(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(top = 50.dp),
                            composition = composition,
                            progress = { animationState.progress }
                        )

                        // Phone Number Input
                        OutlinedTextField(
                            value = mobileNo,
                            onValueChange = { newText ->
                                if (newText.length <= 10 && newText.all { it.isDigit() }) {
                                    mobileNo = newText
                                }
                            },
                            label = { Text("Mobile Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 30.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            maxLines = 1,
                            singleLine = true,
                            prefix = {
                                Text(
                                    "+91", color = Color.Black,
                                    modifier = Modifier.padding(end = 15.dp)
                                )
                            },
                            suffix = {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Mobile No"
                                )
                            }
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        if(mobileNo.length > 10){
                                            snackBarHostState.showSnackbar("Please Enter the Mobil No.")
                                            return@launch
                                        }
                                        val result =
                                            AuthRepository.checkUserAvailable(phoneNumber = "+91$mobileNo")
                                        if (result.first) {
                                            val otpSent = AuthRepository.sendOtp(
                                                phoneNumber = "+91$mobileNo",
                                                isForgotPassword = true
                                            )
                                            if (otpSent) {
                                                snackBarHostState.showSnackbar("OTP is Sent")
                                                showOtpAuthScreen = true
                                            } else {
                                                snackBarHostState.showSnackbar("OTP Not Sent, Try Again.")
                                            }
                                            isLoading = false
                                        } else {
                                            isLoading = false
                                            scope.launch {
                                                result.second?.let { showSnackBar(it) }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            awaitFirstDown()
                                            keyboardController?.hide()
                                        }
                                    }
                            ) {
                                Text(text = "Send OTP")
                            }

                            TextButton(
                                onClick = { navController.navigate(Routes.RegistrationPage) },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "Don't have an Account? Register")
                            }
                        }

                    }
                } else {
                    ResetPassScreen(
                        "+91$mobileNo",
                        coroutineScope = scope,
                        showSnackBar = {
                            scope.launch {
                                showSnackBar(it)
                            }
                        },
                        isPasswordReset = {
                            navController.navigate(Routes.LoginPage)
                        }
                    )
                }
            }

            if(showOtpAuthScreen){
                OtpVerificationScreen(
                    phoneNumber = "+91$mobileNo",
                    coroutineScope = scope,
                    showSnackBar = { scope.launch { snackBarHostState.showSnackbar(it) }},
                    isOtpAuthenticated = {
                        showOtpAuthScreen = false
                        showPasswordResetScreen = true
                    }
                )
            }
        }
    }
}