package com.pepdeal.infotech.login.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.login.viewModel.LoginUiAction
import com.pepdeal.infotech.login.viewModel.LoginUiState
import com.pepdeal.infotech.login.viewModel.LoginViewModal
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.visibility
import pepdealios.composeapp.generated.resources.visibilityoff
import utils.KottieConstants

@Composable
fun LoginScreenRoot(viewModel: LoginViewModal = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackBarMessage) {
        state.snackBarMessage?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
                viewModel.onAction(LoginUiAction.OnClearSnackBar)
            }
        }
    }

    LoginScreen(
        uiState = state,
        snackBarHostState = snackBarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    snackBarHostState: SnackbarHostState,
    onAction: (LoginUiAction) -> Unit,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var animation by remember { mutableStateOf("") }

    // constants
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current


    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/login_anim.json").decodeToString()
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

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                CustomSnackBarHost(
                    snackBarHostState,
                    currentMessage = uiState.snackBarMessage
                )
            }
        ) {
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
                    value = uiState.mobileNo,
                    onValueChange = { newText ->
                        if (newText.length <= 10 && newText.all { it.isDigit() }) {
                            onAction(LoginUiAction.OnMobileNoChange(newText))
                        }
                    },
                    label = { Text("Mobile Number") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Phone")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    maxLines = 1,
                    singleLine = true,
                )

                // Password Input
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { onAction(LoginUiAction.OnPassChange(it)) },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password
                    ),
                    maxLines = 1,
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(if (isPasswordVisible) Res.drawable.visibility else Res.drawable.visibilityoff),
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )


                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .align(Alignment.CenterHorizontally)
                        // Use state to control visibility
                    )
                } else {
                    // Login Button
                    Button(
                        onClick = {
                            onAction(LoginUiAction.OnSubmitClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                // Forgot Password Text
                Text(
                    text = "Forgot Password?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { NavigationProvider.navController.navigate(Routes.ForgetPasswordPage) },
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )

                // Register Text
                Text(
                    text = "Don't have an account? Register here",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                        .clickable(onClick = { NavigationProvider.navController.navigate(Routes.RegistrationPage) }),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        }
    }
}
