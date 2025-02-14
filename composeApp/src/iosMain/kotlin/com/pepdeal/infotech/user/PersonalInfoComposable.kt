package com.pepdeal.infotech.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.profile.ProfileImageSelector
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.ViewModals
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    userId: String,
    viewModal: PersonalInfoViewModal = ViewModals.personalInfoViewModal
) {

    val userDetails = viewModal.userDetails.collectAsStateWithLifecycle()
    val isLoading = viewModal.isLoading.collectAsStateWithLifecycle()
    val uploading = viewModal.uploading.collectAsStateWithLifecycle()

    var phoneNumber by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var profileImage = remember { mutableStateOf<ImageBitmap?>(null) }
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val medialPickerFactory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { medialPickerFactory.createMediaPickerController() }

    val snackBar = remember { SnackbarHostState() }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(userId) {
        println("fetching user")
        viewModal.fetchUserDetails(userId)
    }

    LaunchedEffect(userDetails.value) {
        println(userDetails.value)
        phoneNumber = userDetails.value.mobileNo
        username = userDetails.value.userName
        email = userDetails.value.emailId
    }

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Personal Info",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModal.reset()
                            NavigationProvider.navController.popBackStack()
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
                    ),
                    modifier = Modifier.shadow(4.dp),
                    expandedHeight = 50.dp
                )

                Box(Modifier.fillMaxSize()) {

                    if (isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        keyboardController?.hide()
                                    })
                                },
//                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(50.dp))

                            Card(
                                modifier = Modifier
                                    .size(120.dp),
                                shape = RoundedCornerShape(70.dp),
                                elevation = CardDefaults.cardElevation(0.dp),
                                border = BorderStroke(1.dp, Color.Black)
                            ) {
                                ProfileImageSelector(
                                    imageState = profileImage,
                                    controller = controller,
                                    picker = picker,
                                    snackBar = snackBar
                                )
                            }

                            OutlinedTextField(
                                value = username.trim(),
                                onValueChange = { username = it },
                                enabled = false,
                                label = { Text(text = "Enter Your Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, start = 30.dp, end = 30.dp)
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { newText ->
                                    if (newText.length <= 10 && newText.all { it.isDigit() }) {
                                        phoneNumber = newText
                                    }
                                },
                                enabled = false,
                                label = { Text(text = "Enter Mobile No") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, start = 30.dp, end = 30.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = email.trim(),
                                onValueChange = { email = it },
                                label = { Text(text = "Enter Your Email id") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, start = 30.dp, end = 30.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                            )

                            if(uploading.value){
                                CircularProgressIndicator()
                            }else{
                                Button(onClick = {
                                    viewModal.updateUserEmailId(userId,email, onSuccess = {
                                        if(it){
                                            viewModal.reset()
                                            NavigationProvider.navController.popBackStack()
                                        }
                                    })
                                }){
                                    Text(text = "Upload", fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}