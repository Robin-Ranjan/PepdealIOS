package com.pepdeal.infotech.shopVideo.favShopVideo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.PlayerSpeed
import chaintech.videoplayer.model.ScreenResize
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.super_shop_positive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteShopVideoScreen(
    userId: String,
    viewModal: FavoriteShopVideoViewModal = ViewModals.favoriteShopVideoViewModal
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val shopVideos by viewModal.shopVideos.collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentlyPlayingIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        viewModal.fetchShopVideos(userId)
    }

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Favourite Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModal.resetProduct()
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->

                                }
                            },
                        state = listState
                    ) {
                        itemsIndexed(items = shopVideos) { index, shopVideo ->
                            val isPlaying = index == currentlyPlayingIndex

                            FavoriteShopVideCard(shopVideo,
                                isPlaying = isPlaying,
                                onSaveVideoClicked = {
                                    coroutineScope.launch {
                                        viewModal.removeFavVideo(
                                            userId,
                                            shopVideo.shopVideosMaster.shopId
                                        )
                                    }
                                },
                                onVideoClicked = {
                                    currentlyPlayingIndex = index
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteShopVideCard(
    shopVideo: ShopVideoWithShopDetail,
    isPlaying: Boolean,
    onSaveVideoClicked: () -> Unit,
    onVideoClicked: () -> Unit
) {

    // Player Host for VideoPlayerComposable
    val playerHost = remember {
        MediaPlayerHost(
            isPaused = true,
            isMuted = true,
            initialSpeed = PlayerSpeed.X1,
            initialVideoFitMode = ScreenResize.FILL,
            isLooping = true,
            isFullScreen = false,

            )
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            playerHost.play()
        } else {
            playerHost.pause()
        }
    }
    // Set up the URL
    playerHost.loadUrl(shopVideo.shopVideosMaster.videoUrl)

    // Card Layout in Compose
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Video Container with play button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.DarkGray)
                    .clickable {
//                        isVideoPlaying = !isVideoPlaying
//                        playerHost.play()
                        onVideoClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Video Player
                if (isPlaying) {
                    VideoPlayerComposable(
                        modifier = Modifier.fillMaxSize(),
                        playerHost = playerHost,
                        playerConfig = VideoPlayerConfig(
                            isSeekBarVisible = false,
                            isDurationVisible = true,
                            isScreenLockEnabled = false
                        )
                    )
                } else {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageModel = { shopVideo.shopVideosMaster.thumbNailUrl },
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
            }

            // Shop Details Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .clickable {
                            NavigationProvider.navController.navigate(
                                Routes.ShopDetails(
                                    shopVideo.shopsMaster.shopId ?: "",
                                    Objects.USER_ID
                                )
                            )
                        }
                        .padding(start = 8.dp, top = 3.dp, bottom = 3.dp)
                ) {
                    Text(
                        text = shopVideo.shopsMaster.shopName?.toNameFormat() ?: "",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = shopVideo.shopsMaster.shopAddress ?: "",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = FontFamily(Font(Res.font.manrope_light))
                        ),
                        maxLines = 2,
                        color = Color.Black,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                IconButton(
                    onClick = { onSaveVideoClicked() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.super_shop_positive),
                        contentDescription = "Save Video",
                    )
                }
            }
        }
    }
}