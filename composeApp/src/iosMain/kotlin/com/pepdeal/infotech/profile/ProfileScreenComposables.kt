package com.pepdeal.infotech.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.DefaultAspectRatios
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.core.utils.toImageBitmap
import com.attafitamim.krop.ui.ImageCropperDialog
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.requestPermission
import com.pepdeal.infotech.util.NavigationProvider
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.arrow_forward
import pepdealios.composeapp.generated.resources.baseline_edit_document_24
import pepdealios.composeapp.generated.resources.baseline_person_24
import pepdealios.composeapp.generated.resources.baseline_power_settings_new_24
import pepdealios.composeapp.generated.resources.baseline_video
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.shopping_bag
import pepdealios.composeapp.generated.resources.super_shop_logo
import pepdealios.composeapp.generated.resources.support
import pepdealios.composeapp.generated.resources.tickets
import platform.UIKit.UIImage


@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()
    var profileImage = remember { mutableStateOf<ImageBitmap?>(null) }
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val medialPickerFactory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { medialPickerFactory.createMediaPickerController() }
    val snackBar = remember { SnackbarHostState() }
    MaterialTheme {
        BindEffect(controller)
        BindMediaPickerEffect(picker)
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBar, snackbar = { data ->
                    Snackbar(content = { Text(text = data.visuals.message) })
                })
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(color = Color.White)
            ) {
                // Title Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, top = 0.dp)
                    ) {
                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Scrollable Column
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = 10.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Profile Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.Center),  // Centers both vertically and horizontally
                            verticalArrangement = Arrangement.Center,  // Vertically center the content inside the Column
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(120.dp),
                                shape = RoundedCornerShape(70.dp),
                                elevation = CardDefaults.cardElevation(0.dp),
                                border = BorderStroke(1.dp, Color.Black)
                            ) {
//                            Image(
//                                painter = painterResource(Res.drawable.baseline_person_24),
//                                contentDescription = "Profile Picture",
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
                                ProfileImageSelector(
                                    imageState = profileImage,
                                    controller = controller,
                                    picker = picker,
                                    snackBar = snackBar
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Username",
                                fontSize = 15.sp,
                                color = Color.Black,
                                lineHeight = 15.sp
                            )

                            Text(
                                text = "+123 456 7890",
                                fontSize = 11.sp,
                                color = Color.Black,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Customer Page",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                    Spacer(Modifier.height(5.dp))
                    // Other menu items
                    ProfileMenuItem(
                        text = "Favorites",
                        icon = Res.drawable.black_heart,
                        onClick = { NavigationProvider.navController.navigate(Routes.FavouritesPage) })
                    ProfileMenuItem(
                        text = "Super Shop",
                        icon = Res.drawable.super_shop_logo,
                        onClick = { println("Favorites") })
                    ProfileMenuItem(
                        text = "Tickets",
                        icon = Res.drawable.tickets,
                        onClick = { NavigationProvider.navController.navigate(Routes.CustomerTicketPage) })
                    ProfileMenuItem(
                        text = "Saved Shop Video",
                        icon = Res.drawable.super_shop_logo,
                        onClick = { println("Favorites") })
                    ProfileMenuItem(
                        text = "Open Your Shop",
                        icon = Res.drawable.shopping_bag,
                        onClick = { NavigationProvider.navController.navigate(Routes.OpenYourShopPage) })
                    ProfileMenuItem(
                        text = "Personal Info",
                        icon = Res.drawable.baseline_person_24,
                        onClick = { })
                    Text(
                        text = "Seller Page",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp)
                    )
                    Spacer(Modifier.height(5.dp))
                    ProfileMenuItem(
                        text = "List Product",
                        icon = Res.drawable.shopping_bag,
                        onClick = { NavigationProvider.navController.navigate(Routes.ListProductPage) })

                    ProfileMenuItem(
                        text = "Update Listing",
                        icon = Res.drawable.shopping_bag,
                        onClick = { println("Favorites") })

                    ProfileMenuItem(
                        text = "Tickets",
                        icon = Res.drawable.tickets,
                        onClick = { println("Favorites") })

                    ProfileMenuItem(
                        text = "Edit Shop Details",
                        icon = Res.drawable.shopping_bag,
                        onClick = { println("Favorites") })

                    ProfileMenuItem(
                        text = "Shop Video",
                        icon = Res.drawable.baseline_video,
                        onClick = { println("Favorites") })

                    Text(
                        text = "Support & FAQ",
                        color = Color.Black,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp)
                    )
                    Spacer(Modifier.height(5.dp))
                    ProfileMenuItem(
                        text = "Support",
                        icon = Res.drawable.support,
                        onClick = { println("Favorites") })
                    ProfileMenuItem(
                        text = "Legal",
                        icon = Res.drawable.baseline_edit_document_24,
                        onClick = { println("Favorites") })
                    ProfileMenuItem(
                        text = "About Us",
                        icon = Res.drawable.support,
                        onClick = { println("Favorites") })
                    // Logout Card
                    LogoutCard {
                        if (it) NavigationProvider.navController.navigate(Routes.LoginPage)
                    }

                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(text: String, icon: DrawableResource, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge
        )

        Icon(
            painter = painterResource(Res.drawable.arrow_forward),
            contentDescription = "Arrow",
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
    }
}

@Composable
fun LogoutCard(onClick: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(1f) // Set width percentage as per your constraint in XML
            .padding(horizontal = 5.dp, vertical = 15.dp)
            .clickable(onClick = { onClick(true) }), // Handles the click event
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = CardDefaults.outlinedCardBorder(true)// Set background color of the card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), // Padding inside the card
            horizontalArrangement = Arrangement.Center, // Horizontally center the items
            verticalAlignment = Alignment.CenterVertically // Vertically center the items
        ) {
            Icon(
                painter = painterResource(Res.drawable.baseline_power_settings_new_24), // Replace with your icon
                contentDescription = "Logout Icon",
                modifier = Modifier.size(24.dp), // Set size of the icon
                tint = Color.Black // Set tint color for the icon
            )
            Spacer(modifier = Modifier.width(10.dp)) // Spacer between icon and text
            Text(
                text = "Login",
                fontSize = 17.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileImageSelector(
    imageState: MutableState<ImageBitmap?>,
    controller: PermissionsController,
    picker: MediaPickerController,
    snackBar: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()

    val cropState = imageCropper.cropState
    if (cropState != null) {
        ImageCropperDialog(
            state = cropState,
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
            style = cropperStyle(
                backgroundColor = Color.Yellow,
                autoZoom = false,
                aspects = DefaultAspectRatios
            )
        )
    }

    // Use ImageBitmap if available, otherwise fallback to Painter
    if (imageState.value != null) {
        Image(
            bitmap = imageState.value!!,
            contentDescription = "Profile Image",
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    scope.launch {
                        requestPermission(
                            controller,
                            permission = Permission.GALLERY,
                            snackBar,
                            picker,
                            imageState = { imageBitMap ->
                                scope.launch {
                                    imageBitMap.let {
                                        val result = imageCropper.crop(
                                            maxResultSize = IntSize(1200, 1200),
                                            bmp = imageBitMap
                                        ) // Suspends until user accepts or cancels cropping
                                        val croppedBitmap = when (result) {
                                            CropResult.Cancelled -> null
                                            is CropError -> null
                                            is CropResult.Success -> result.bitmap
                                        }
                                        croppedBitmap?.let { imageState.value = it }
                                    }
                                }
                            }
                        )
                    }
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
    } else {
        Image(
            painter = painterResource(Res.drawable.baseline_person_24),
            contentDescription = "Profile Image",
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    scope.launch {
                        requestPermission(
                            controller,
                            permission = Permission.GALLERY,
                            snackBar,
                            picker,
                            imageState = { imageBitMap ->
                                scope.launch {
                                    imageBitMap.let {
                                        val result = imageCropper.crop(
                                            maxResultSize = IntSize(1200, 1200),
                                            bmp = imageBitMap
                                        ) // Suspends until user accepts or cancels cropping
                                        val croppedBitmap = when (result) {
                                            CropResult.Cancelled -> null
                                            is CropError -> null
                                            is CropResult.Success -> result.bitmap
                                        }
                                        croppedBitmap?.let { imageState.value = it }
                                    }
                                }
                            }
                        )
                    }
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
    }
}
