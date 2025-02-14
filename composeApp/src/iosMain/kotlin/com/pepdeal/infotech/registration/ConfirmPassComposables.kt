package com.pepdeal.infotech.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.delay
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pepdealios.composeapp.generated.resources.Res
import utils.KottieConstants

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ConfirmPassScreen(showSnackBar: (String) -> Unit,){
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var confirmPassAnimation by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    val confirmComposition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(confirmPassAnimation)
    )

    val conPassAnimationState by animateKottieCompositionAsState(
        composition = confirmComposition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        try {
            confirmPassAnimation = Res.readBytes("files/cnf_a.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Lottie Animation
        KottieAnimation(
            modifier = Modifier
                .size(373.dp, 270.dp),
            composition = confirmComposition,
            progress = { conPassAnimationState.progress }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { /* Toggle password visibility */ }) {
                    Icon(Icons.Default.Email, contentDescription = "Toggle Password")
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Confirm Password Input
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { /* Toggle password visibility */ }) {
                    Icon(Icons.Default.Person, contentDescription = "Toggle Password")
                }
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Reset Password Button
        Button(
            onClick = {
                keyboardController?.hide()
                isLoading = true
                if(validatePass(password,confirmPassword)){
                    showSnackBar("Same Pass")
                }else{
                    showSnackBar("Pass mismatched")
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Reset Password", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Progress Bar
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

fun validatePass(pass:String,confPass:String):Boolean{
    return pass == confPass
}