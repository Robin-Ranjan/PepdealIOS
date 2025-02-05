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
import androidx.compose.material.icons.filled.Call
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
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import pepdealios.composeapp.generated.resources.Res


@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by remember { mutableStateOf(false) }

    var animation by remember { mutableStateOf("") }

    LaunchedEffect(Unit){
        try {
            animation = Res.readBytes("raw/login_amin.json").decodeToString()
//        animation = readResourceBytes("files/animation.json").decodeToString()
        }catch (e:Exception){
            println(e.message)
        }
    }

    val composition = rememberKottieComposition(
        spec = KottieCompositionSpec.File(animation) // Or KottieCompositionSpec.Url || KottieCompositionSpec.JsonString
    )

    var playing by remember { mutableStateOf(false) }

    val animationState by animateKottieCompositionAsState(
        composition = composition,
        isPlaying = playing
    )
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 30.dp)
                .pointerInput(Unit){
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
                progress = {animationState.progress}
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


            // Login Button
            Button(
                onClick = onLoginClick,
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
                    .clickable(onClick = onRegisterClick),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )

            // Loading Progress Bar

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .align(Alignment.CenterHorizontally)
                    // Use state to control visibility
                )
            }
        }
    }
}
