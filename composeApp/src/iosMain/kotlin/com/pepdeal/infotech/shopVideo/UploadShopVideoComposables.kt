package com.pepdeal.infotech.shopVideo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.model.ScreenResize
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import com.pepdeal.infotech.profile.ProfileImageSelector
import com.pepdeal.infotech.util.NavigationProvider
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaType
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.media.picker.CanceledException
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadShopVideoScreen() {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val thumbNailImage = remember { mutableStateOf<ImageBitmap?>(null) }
    val medialPickerFactory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { medialPickerFactory.createMediaPickerController() }
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    enableBackGestureForNavigationController()

    val thumbNailUrl by remember { mutableStateOf("") }
    val videoUrl by remember { mutableStateOf("") }
    // Player Host for VideoPlayerComposable
    val playerHost = remember {
        MediaPlayerHost(
            isPaused = true,
            isMuted = true,
            initialSpeed = PlayerSpeed.X1,
            initialVideoFitMode = ScreenResize.FILL,
            isLooping = true,
            isFullScreen = false
        )
    }

    var selectedVideoPath by remember { mutableStateOf("") }
    LaunchedEffect(selectedVideoPath) {
        if (selectedVideoPath.isNotEmpty()) {
            playerHost.pause()
            playerHost.loadUrl(selectedVideoPath)
            playerHost.play()
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
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = {
                        androidx.compose.material.Text(
                            text = "Shop Video",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
//                                viewModal.resetTicket()
                                NavigationProvider.navController.popBackStack()
                            }
                        ) {
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
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Note: Video size should be up to 1 min and less than 10MB.",
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedVideoPath.isEmpty()) {
                                Text(
                                    text = "Tap to select a video",
                                    color = Color.White,
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            requestPermissionForVideo(
                                                controller = controller,
                                                permission = Permission.GALLERY,
                                                snackBar,
                                                picker
                                            ) { media ->
                                                selectedVideoPath = media
                                            }
                                        }
                                    })
                            } else {
                                VideoPlayerComposable(
                                    modifier = Modifier.fillMaxSize(),
                                    playerHost = playerHost,
                                    playerConfig = VideoPlayerConfig(
                                        isSeekBarVisible = false,
                                        isDurationVisible = true,
                                        isScreenLockEnabled = false,
                                        isScreenResizeEnabled = false,
                                        isFullScreenEnabled = true
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.sdp))
                        // Thumbnail Image (Optional)

                        if (thumbNailUrl.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                            ) {
                                ProfileImageSelector(
                                    imageState = thumbNailImage,
                                    controller = controller,
                                    picker = picker,
                                    snackBar = snackBar,
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        } else {
                            CoilImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(color = Color.Gray),
                                imageModel = { thumbNailUrl.trim() },
                                imageOptions = ImageOptions(
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                        setToSaturation(1f)
                                    })
                                ),
                                previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
                            )
                        }

                        // Validation Status
                        Text(
                            text = "", // Add validation status logic here
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    requestPermissionForVideo(
                                        controller = controller,
                                        permission = Permission.GALLERY,
                                        snackBar,
                                        picker
                                    ) { media ->
                                        selectedVideoPath = media
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(text = "Select Another Video")
                        }

                        Button(
                            onClick = {
                                if((thumbNailImage.value != null || thumbNailUrl.isNotEmpty()) && (selectedVideoPath.isNotEmpty() || videoUrl.isNotEmpty())){
                                    scope.launch {
                                        val result =
                                            UploadShopVideoRepo().validateVideo(selectedVideoPath)
                                        snackBar.showSnackbar(result.message)
                                    }
                                }else{
                                    scope.launch {
                                        snackBar.showSnackbar("Select video and image both")
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(text = "Submit")
                        }
                    }
                }
            }
        }
    }
}

suspend fun requestPermissionForVideo(
    controller: PermissionsController,
    permission: Permission,
    snackBarHostState: SnackbarHostState,
    picker: MediaPickerController,
    onFileSelected: (String) -> Unit // Callback to pass the selected file
) {
    // Check if permission is granted
    val isGranted = controller.isPermissionGranted(permission)
    if (isGranted) {
        try {
            // Pick a file (image/video) from the gallery
            val pickedMedia = picker.pickMedia()
//            MediaType.VIDEO

            // Check if the picked media is a video
            if (isVideoFile(pickedMedia)) {
                // If it's a video, pass the file path to the callback
                onFileSelected(pickedMedia.path)
            } else {
                // If it's not a video, show a snackBar message
                snackBarHostState.showSnackbar(
                    "Selected file is not a video",
                    duration = SnackbarDuration.Short
                )
            }
        } catch (e: CanceledException) {
            snackBarHostState.showSnackbar("No media selected", duration = SnackbarDuration.Short)
        }
    } else {
        // Request permission if not granted
        try {
            val permissionState = controller.getPermissionState(permission)
            when (permissionState) {
                PermissionState.Denied -> controller.providePermission(permission)
                PermissionState.DeniedAlways -> {
                    val result = snackBarHostState.showSnackbar(
                        message = "Permission denied. Open settings to grant it.",
                        actionLabel = "Settings",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        controller.openAppSettings()
                    }
                }

                else -> controller.providePermission(permission)
            }
        } catch (e: DeniedException) {
            snackBarHostState.showSnackbar("Permission denied", duration = SnackbarDuration.Short)
        } catch (e: DeniedAlwaysException) {
            val result = snackBarHostState.showSnackbar(
                message = "Permission denied. Open settings to grant it.",
                actionLabel = "Settings",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }
        } catch (e: RequestCanceledException) {
            snackBarHostState.showSnackbar("Permission request canceled")
        }
    }
}

fun isVideoFile(media: Media): Boolean {
    return media.type == MediaType.VIDEO
}

//fun pickVideoFile(onVideoSelected: (String) -> Unit) {
//    val controller = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTType.load()), asReplacementFor = null)
//
//    controller.didPickDocumentAtURLs = { urls ->
//        val videoUrl = urls.firstOrNull()?.absoluteString
//        if (videoUrl != null) {
//            onVideoSelected(videoUrl)
//        }
//    }
//    // Present the controller
//    controller.presentViewController(animated = true)
//}


fun enableBackGestureForNavigationController() {
    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootController is UINavigationController) {
        rootController.interactivePopGestureRecognizer?.enabled = true
    }
}


