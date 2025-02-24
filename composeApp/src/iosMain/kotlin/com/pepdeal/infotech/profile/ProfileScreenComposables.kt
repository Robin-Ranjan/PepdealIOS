package com.pepdeal.infotech.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.DefaultAspectRatios
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.ProfileScreenViewModal
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.requestPermission
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.arrow_forward
import pepdealios.composeapp.generated.resources.baseline_edit_document_24
import pepdealios.composeapp.generated.resources.baseline_person_24
import pepdealios.composeapp.generated.resources.baseline_power_settings_new_24
import pepdealios.composeapp.generated.resources.baseline_video
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.shopping_bag
import pepdealios.composeapp.generated.resources.super_shop_logo
import pepdealios.composeapp.generated.resources.support
import pepdealios.composeapp.generated.resources.tickets


@Composable
fun ProfileScreen(viewModal: ProfileScreenViewModal = ViewModals.profileScreenViewModal) {
    val datastore = DataStore.dataStore
    val preferences by datastore.data.collectAsState(initial = emptyPreferences())

    val currentUserId by datastore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")

    val userStatus by datastore.data.map { it[PreferencesKeys.USER_STATUS] ?: "-1" }
        .collectAsState(initial = "-1")

    val userName by datastore.data.map { it[PreferencesKeys.USER_NAME] ?: "-1" }
        .collectAsState(initial = "-1")
    val userPhone by datastore.data.map { it[PreferencesKeys.MOBILE_NO] ?:"-1" }
        .collectAsState(initial = "-1")

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val profileImage = remember { mutableStateOf<ImageBitmap?>(null) }
    val profileImageUrl by viewModal.userProfilePicMaster.collectAsStateWithLifecycle()
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }

    val medialPickerFactory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { medialPickerFactory.createMediaPickerController() }
    val snackBar = remember { SnackbarHostState() }

    LaunchedEffect(profileImageUrl, currentUserId) {
        if (profileImageUrl?.profilePicUrl.isNullOrEmpty() && currentUserId != "-1") {
            println("Fetching profile picture")
            viewModal.fetchUserProfilePic(currentUserId)
        }
    }

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

                                ProfileImageSelector(
                                    imageState = profileImage,
                                    controller = controller,
                                    picker = picker,
                                    snackBar = snackBar,
                                    imageUrl = profileImageUrl?.profilePicUrl ?: ""
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if(userName!= "-1" && userPhone != "-1"){
                                Text(
                                    text = userName.toNameFormat(),
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                    lineHeight = 15.sp
                                )

                                Text(
                                    text = userPhone,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    lineHeight = 15.sp
                                )
                            }
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
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.SuperShopPage(
                                    userId = currentUserId
                                )
                            )
                        })
                    ProfileMenuItem(
                        text = "Tickets",
                        icon = Res.drawable.tickets,
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.CustomerTicketPage(
                                    currentUserId
                                )
                            )
                        })
                    ProfileMenuItem(
                        text = "Saved Shop Video",
                        icon = Res.drawable.super_shop_logo,
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.FavoriteShopVideosPage(
                                    currentUserId
                                )
                            )
                        })
                    if (userStatus == "1") {
                        ProfileMenuItem(
                            text = "Your Shop",
                            icon = Res.drawable.shopping_bag,
                            onClick = { NavigationProvider.navController.navigate(Routes.OpenYourShopPage) })
                    } else {
                        ProfileMenuItem(
                            text = "Open Your Shop",
                            icon = Res.drawable.shopping_bag,
                            onClick = { NavigationProvider.navController.navigate(Routes.OpenYourShopPage) })
                    }

                    ProfileMenuItem(
                        text = "Personal Info",
                        icon = Res.drawable.baseline_person_24,
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.PersonalInfoPage(
                                    userId = currentUserId
                                )
                            )
                        })
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
                        onClick = { NavigationProvider.navController.navigate(Routes.AddNewProductPage) })

                    ProfileMenuItem(
                        text = "Update Listing",
                        icon = Res.drawable.shopping_bag,
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.ListAllProductPage(
                                    Objects.SHOP_ID
                                )
                            )
                        })

                    ProfileMenuItem(
                        text = "Tickets",
                        icon = Res.drawable.tickets,
                        onClick = { NavigationProvider.navController.navigate(Routes.SellerTicketPage) })

                    ProfileMenuItem(
                        text = "Edit Shop Details",
                        icon = Res.drawable.shopping_bag,
                        onClick = {
                            NavigationProvider.navController.navigate(
                                Routes.EditShopDetails(
                                    "-OG9iDx7RKUPZ6RHwsIA",
                                    currentUserId
                                )
                            )
                        })

                    ProfileMenuItem(
                        text = "Shop Video",
                        icon = Res.drawable.baseline_video,
                        onClick = { NavigationProvider.navController.navigate(Routes.UploadShopVideoPage) })

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
                    LogoutCard(
                        userId = currentUserId,
                        onClick = {
                            NavigationProvider.navController.navigate(Routes.LoginPage) {
                                popUpTo(Routes.MainPage) {
                                    inclusive = true
                                } // Clears MainPage and navigates fresh to Login
                            }
                        },
                        onLogout = {
                            scope.launch {
                                datastore.edit { it.clear() } // Clear stored user session
                            }
                            viewModal.reset()
//                            NavigationProvider.navController.navigate(Routes.LoginPage) {
//                                popUpTo(Routes.MainPage) { inclusive = true }
//                            }
                        }
                    )
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
fun LogoutCard(userId: String, onClick: (Boolean) -> Unit, onLogout: () -> Unit) {
//    val userId = preferences[PreferencesKeys.USERID_KEY] ?: "-1"  // Directly observe

    Card(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(horizontal = 5.dp, vertical = 15.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable(onClick = { if (userId == "-1") onClick(true) else onLogout() }),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.baseline_power_settings_new_24),
                contentDescription = "Logout Icon",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (userId == "-1") "Login" else "Logout",
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
    snackBar: SnackbarHostState,
    painter: Painter = painterResource(Res.drawable.baseline_person_24),
    contentScale: ContentScale = ContentScale.Fit,
    imageUrl: String = ""
) {
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()

    // Show cropping dialog if cropping is active.
    imageCropper.cropState?.let { cropState ->
        ImageCropperDialog(
            state = cropState,
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
            style = cropperStyle(
                backgroundColor = Color.Yellow,
                autoZoom = false,
                aspects = DefaultAspectRatios
            )
        )
    }

    // Consolidated click handler: Request permission, pick image, crop it, and update imageState.
    val onImageClick = {
        scope.launch {
            requestPermission(
                controller = controller,
                permission = Permission.GALLERY,
                snackBarHostState = snackBar,
                picker = picker,
                imageState = { newImage ->
                    scope.launch {
                        newImage.let { image ->
                            // Launch cropper with desired max size.
                            val result = imageCropper.crop(
                                maxResultSize = IntSize(1200, 1200),
                                bmp = image
                            )
                            val croppedBitmap = when (result) {
                                CropResult.Cancelled -> null
                                is CropError -> null
                                is CropResult.Success -> result.bitmap
                            }
                            croppedBitmap?.let {
                                imageState.value = it
                            }
                        }
                    }
                }
            )
        }
    }

    // Helper composable: Determines which image to display based on state and imageUrl.
    @Composable
    fun DisplayImage() {
        when {
            imageState.value != null -> {
                Image(
                    bitmap = imageState.value!!,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }

            imageUrl.isNotEmpty() -> {
                CoilImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .border(width = 1.dp, color = Color.Gray),
                    imageModel = { imageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    previewPlaceholder = painterResource(Res.drawable.compose_multiplatform),
                )
            }

            else -> {
                Image(
                    painter = painter,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = Alignment.Center
                )
            }
        }
    }

    // A Box wrapping the image display that applies the same onClick action regardless of content.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onImageClick() }
    ) {
        DisplayImage()
    }
}


