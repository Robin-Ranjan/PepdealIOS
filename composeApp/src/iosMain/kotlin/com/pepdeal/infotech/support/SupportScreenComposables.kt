package com.pepdeal.infotech.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.shop.TextFieldWithLabel
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util.isValidEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(userName: String, userPhoneNo: String) {
    val subject = remember { mutableStateOf(TextFieldValue()) }
    val email = remember { mutableStateOf(TextFieldValue()) }
    val query = remember { mutableStateOf(TextFieldValue()) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    var isUploading by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Contact Us",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
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
                                tint = Color.Black
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
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GET IN TOUCH!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            TextFieldWithLabel(
                                label = "Subject",
                                maxLines = 1,
                                state = subject,
                            )

                            TextFieldWithLabel(
                                label = "E-mail",
                                state = email,
                                inputType = KeyboardType.Email
                            )
                            TextFieldWithLabel(
                                label = "Type Your Query here...",
                                state = query,
                                minLines = 4,
                                maxLines = 10
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isUploading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        validateQueryAndSubmit(
                                            name = userName,
                                            mobileNo = userPhoneNo,
                                            email = email.value.text,
                                            subject = subject.value.text,
                                            query = query.value.text,
                                            setError = { errorMessage ->
                                                scope.launch {
                                                    if (errorMessage.isNotBlank() && errorMessage.isNotEmpty()) {
                                                        snackBarHostState.showSnackbar(errorMessage)
                                                    }
                                                }
                                                isUploading = false
                                            },
                                            status = { isValid -> isUploading = isValid },
                                            onSubmit = {
                                                isUploading = true
                                                val result = GoogleFormService().submitToGoogleForm(
                                                    name = userName,
                                                    mobileNo = userPhoneNo,
                                                    email = email.value.text,
                                                    subject = subject.value.text,
                                                    query = query.value.text
                                                )

                                                if (result) {
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("Query is Submitted.")
                                                    }
                                                    navController.popBackStack()
                                                } else {
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("Something went wrong")
                                                    }
                                                    isUploading = false
                                                }
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                ) {
                                    Text(
                                        text = "Submit",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun validateQueryAndSubmit(
    name: String,
    mobileNo: String,
    email: String,
    subject: String,
    query: String,
    setError: (String) -> Unit,
    status: (Boolean) -> Unit,
    onSubmit: suspend () -> Unit
) {
    when {
        name.isBlank() -> setError("Name cannot be empty!")
        mobileNo.isBlank() -> setError("Phone number cannot be empty!")
        mobileNo.length < 10 -> setError("Phone number must be at least 10 digits long!")
        email.isBlank() -> setError("Email cannot be empty!")
        !isValidEmail(email) -> setError("Invalid email format!")
        subject.isBlank() -> setError("Subject cannot be empty!")
        query.isBlank() -> setError("Query cannot be empty!")
        else -> {
            setError("") // Clear error messages
            status(true)

            // Launch a coroutine in the Main thread (works on both Android & iOS)
            CoroutineScope(Dispatchers.Main).launch {
                onSubmit()
            }
        }
    }
}




