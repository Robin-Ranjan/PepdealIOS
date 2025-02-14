package com.pepdeal.infotech.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.ShopViewModal
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import pepdealios.composeapp.generated.resources.Res
import utils.KottieConstants
import utils.kottieReadBytes


@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModal = ViewModals.loginViewModal,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var animation by remember { mutableStateOf("") }

    val loginStatus by viewModel.loginStatus.collectAsStateWithLifecycle()
    val loginMessage by viewModel.loginMessage.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackBar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/login_anim.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    LaunchedEffect(isLoading){
        if(loginStatus){
            onLoginClick()
        }
    }
    LaunchedEffect(loginMessage){
        if(loginMessage.isNotBlank()){
            snackBar.showSnackbar(loginMessage)
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
                SnackbarHost(hostState = snackBar, snackbar = { data ->
                    Snackbar(
                        content = {
                            Text(text = data.visuals.message)
                        }
                    )
                })
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 30.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            keyboardController?.hide()
                        })
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
                    value = username,
                    onValueChange = { newText ->
                        if (newText.length <= 10 && newText.all { it.isDigit() }) {
                            username = newText
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
                    value = password,
                    onValueChange = { password = it },
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
                                imageVector = if (isPasswordVisible) Icons.Sharp.MoreVert else Icons.Filled.CheckCircle,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )


                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .align(Alignment.CenterHorizontally)
                        // Use state to control visibility
                    )
                }else{
                    // Login Button
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                scope.launch { snackBar.showSnackbar("Please fill in all details") }
                            } else if (username.length < 10) {
                                scope.launch { snackBar.showSnackbar("Mobile no must be at least 10 characters") }
                            } else {
                                viewModel.validateUser("+91$username",Util.hashPassword(password))
                            }
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
                        .clickable { onForgotPasswordClick() },
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
