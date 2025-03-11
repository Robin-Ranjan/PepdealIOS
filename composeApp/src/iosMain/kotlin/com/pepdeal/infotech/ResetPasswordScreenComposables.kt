package com.pepdeal.infotech

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.registration.AuthRepository
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pepdealios.composeapp.generated.resources.Res
import utils.KottieConstants

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ResetPassScreen(
    phoneNumber: String,
    coroutineScope: CoroutineScope,
    showSnackBar: (String) -> Unit,
    isPasswordReset: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confPassword by remember { mutableStateOf("") }
    var animation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    var isPasswordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val animationComposition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(animation)
    )

    val animationState by animateKottieCompositionAsState(
        composition = animationComposition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/cnf_pass_anim.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    MaterialTheme {
        Scaffold {
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
                    composition = animationComposition,
                    progress = { animationState.progress }
                )

                // Phone Number Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
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

                // Confirm Password Input
                OutlinedTextField(
                    value = confPassword,
                    onValueChange = { confPassword = it },
                    label = { Text("Confirm Password") },
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
                    )
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            val result  = validatePasswords(password.trim(),confPassword.trim(), snackBarMessage = {
                                showSnackBar(it)
                            })
                            if(result){
                                scope.launch {
                                  val status =   AuthRepository.updateUserPassword(phoneNumber,Util.hashPassword(password.trim()))
                                    if(status){
                                        isPasswordReset()
                                    }else {
                                        showSnackBar("Something went wrong")
                                    }
                                    isLoading = false
                                }
                            } else{
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Reset",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Outer function for validation
 */
fun validatePasswords(
    password: String,
    confirmPassword: String,
    snackBarMessage: (String) -> Unit
): Boolean {
    return when {
        password.isEmpty() || confirmPassword.isEmpty() -> {
            snackBarMessage("Please fill all the details")
            false
        }

        password != confirmPassword -> {
            snackBarMessage("Password Mismatched")
            false
        }

        else -> true
    }
}