package com.pepdeal.infotech.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.pepdeal_logo_new

@Composable
fun RegisterScreen() {
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isTermAccepted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showOtpScreen by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible = remember { mutableStateOf(false) }


    suspend fun showSnackBar(message: String) {
        snackBarHostState.showSnackbar(message)
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (keyboardVisible.value) 300.dp else 0.dp)
            ) {
                if (!showOtpScreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .verticalScroll(
                                state = rememberScrollState(),
                                flingBehavior = ScrollableDefaults.flingBehavior()
                            )
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    keyboardController?.hide()
                                }
                            }
                    ) {
                        // First View (Logo and Text Fields)
                        Image(
                            painter = painterResource(Res.drawable.pepdeal_logo_new),
                            contentDescription = null,
                            modifier = Modifier
                                .size(300.dp, 80.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 20.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(text = "Enter Your Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { newText ->
                                if (newText.length <= 10 && newText.all { it.isDigit() }) {
                                    phoneNumber = newText
                                }
                            },
                            label = { Text(text = "Enter Mobile No") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            prefix = {
                                Text("+91", color = Color.Black,
                                    modifier = Modifier.padding(end = 15.dp))
                            },
                            suffix = {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Mobile No"
                                )
                            }
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(text = "Enter Your Email id") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(text = "Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(text = "confirm password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Checkbox(
                                checked = isTermAccepted,
                                onCheckedChange = { isTermAccepted = it }
                            )
                            Text(
                                text = "Term and Condition",
                                color = Color.Red,
                                modifier = Modifier
                                    .clickable {
                                        Util.openUrlInBrowser()
                                    },
                                style = TextStyle(textDecoration = TextDecoration.Underline)
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = {
                                    println("userName in handleRegistration:- $username")
                                    handleRegistration(
                                        username = username,
                                        phoneNumber = "+91$phoneNumber",
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        isTermAccepted = isTermAccepted,
                                        coroutineScope = coroutineScope,
                                        snackBarHostState = snackBarHostState,
                                        setIsLoading = { isLoading = it },
                                        setShowOtpScreen = { showOtpScreen = it },
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(text = "Register")
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            TextButton(
                                onClick = { NavigationProvider.navController.navigate(Routes.LoginPage) },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "Already have an Account ?")
                            }
                        }
                    }
                } else {
                    OtpVerificationScreen(phoneNumber,
                        userName = username,
                        coroutineScope,
                        showSnackBar = {
                            coroutineScope.launch {
                                showSnackBar(it)
                            }
                        },
                        isOtpAuthenticated = {
                            coroutineScope.launch {
                               val (status,message) = AuthRepository.registerUser(
                                    userMaster = UserMaster(
                                        userName = username,
                                        mobileNo = "+91$phoneNumber",
                                        emailId = email,
                                        password = password,
                                        isActive = "0",
                                        userStatus = "0",
                                        createdAt = Util.getCurrentTimeStamp(),
                                        updatedAt = Util.getCurrentTimeStamp(),
                                    )
                               )

                                if(status){
                                    NavigationProvider.navController.navigate(Routes.MainPage)
                                    AuthRepository.sendRegistrationSms("+91$phoneNumber")
                                }else{
                                    if (message != null) {
                                        showSnackBar(message)
                                    }
                                }
                            }
                        })

                }
            }
        }
    }
}

/**
 * Outer function for validation
 */
suspend fun validateInfo(
    username: String,
    phoneNumber: String,
    email: String,
    password: String,
    confirmPassword: String,
    isTermAccepted: Boolean,
    snackBarHostState: SnackbarHostState
): Boolean {
    return when {
        username.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
            snackBarHostState.showSnackbar("Please fill all the details")
            false
        }

        phoneNumber.length != 13 -> {
            snackBarHostState.showSnackbar("Enter a valid phone number")
            false
        }

        password != confirmPassword -> {
            snackBarHostState.showSnackbar("Password Mismatched")
            false
        }

        !isTermAccepted -> {
            snackBarHostState.showSnackbar("Please accept terms and conditions")
            false
        }

        else -> true
    }
}

fun handleRegistration(
    username: String,
    phoneNumber: String,
    email: String,
    password: String,
    confirmPassword: String,
    isTermAccepted: Boolean,
    coroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    setIsLoading: (Boolean) -> Unit,
    setShowOtpScreen: (Boolean) -> Unit,
) {
    setIsLoading(true)
    coroutineScope.launch {
        try {
            if (!validateInfo(
                    username,
                    phoneNumber,
                    email,
                    password,
                    confirmPassword,
                    isTermAccepted,
                    snackBarHostState
                )
            ) {
                setIsLoading(false)
                return@launch
            }

            val (userAvailable, message) = AuthRepository.checkUserAvailable(phoneNumber)
            println("$userAvailable $message")
            if (userAvailable) {
                snackBarHostState.showSnackbar("User Found")
                setIsLoading(false)
                return@launch
            }

            println("Sending OTP :- $username")
            val result = AuthRepository.sendOtp(phoneNumber = phoneNumber, userName = username)
            if(result){
                println("Sent OTP")
                setShowOtpScreen(true)  // Switch to OTP screen
            } else {
                println("Failed To Send OTP")
            }

        } catch (e: Exception) {
            snackBarHostState.showSnackbar("Error: ${e.message}")
        } finally {
            setIsLoading(false)
        }
    }
}


