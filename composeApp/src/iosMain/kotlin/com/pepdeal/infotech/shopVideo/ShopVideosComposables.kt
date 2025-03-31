package com.pepdeal.infotech.shopVideo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
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
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.SearchView
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo_new
import pepdealios.composeapp.generated.resources.super_shop_logo
import pepdealios.composeapp.generated.resources.super_shop_positive
import kotlin.math.abs

@Composable
fun FeedScreen(viewModal: ShopVideosViewModal = ViewModals.shopVideosViewModal) {

    val datastore = DataStore.dataStore
    val currentUserId by datastore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")

    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    val shopVideosList by viewModal.shopVideos.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Track favorite states
    val saveShopSates = remember { mutableStateMapOf<String, Boolean>() }
    var currentlyPlayingIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val centerIndex = visibleItems.minByOrNull { abs(it.offset) }?.index ?: -1
        currentlyPlayingIndex = centerIndex
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        keyboardController?.hide()
                    }
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(Res.drawable.pepdeal_logo_new),
                        contentDescription = "Your image description",
                        modifier = Modifier
                            .width(130.dp)
                            .height(28.dp)
                            .padding(start = 5.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                SearchView("Search Shop", searchQuery) {
                    searchQuery = it
                }

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
                    itemsIndexed(shopVideosList) { index, shopVideo ->
                        val isPlaying = index == currentlyPlayingIndex
                        val isSaveVideo = saveShopSates[shopVideo.shopVideosMaster.shopId] ?: false
                        val saveVideoIcon =
                            if (isSaveVideo) Res.drawable.super_shop_positive else Res.drawable.super_shop_logo

                        LaunchedEffect(shopVideo.shopVideosMaster.shopId) {
                            viewModal.checkSaveShopExists(
                                currentUserId,
                                shopVideo.shopVideosMaster.shopId
                            ) {
                                println("${shopVideo.shopsMaster.shopName} $it")
                                saveShopSates[shopVideo.shopVideosMaster.shopId] = it
                            }
                        }
                        FeedCard(shopVideo = shopVideo,
                            superShopRes = painterResource(saveVideoIcon),
                            userId = currentUserId,
                            isPlaying = isPlaying,
                            onSaveVideoClicked = {
                                val newSavedVideoState = !isSaveVideo
                                saveShopSates[shopVideo.shopVideosMaster.shopId] =
                                    newSavedVideoState

                                // Call ViewModel to handle like/unlike logic
                                coroutineScope.launch {
                                    viewModal.toggleSaveShopStatus(
                                        userId = currentUserId,
                                        shopId = shopVideo.shopVideosMaster.shopId,
                                        shopVideoId = shopVideo.shopVideosMaster.shopVideoId,
                                        newSavedVideoState
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

@Composable
fun FeedCard(
    shopVideo: ShopVideoWithShopDetail,
    userId: String,
    superShopRes: Painter,
    isPlaying: Boolean,
    onSaveVideoClicked: () -> Unit,
    onVideoClicked: () -> Unit
) {

    // Player Host for VideoPlayerComposable
    val playerHost = remember {
        MediaPlayerHost(
            isPaused = true,
            isMuted = false,
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
            .padding(),
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
                            isScreenLockEnabled = false,
                            isScreenResizeEnabled = false,
                            isFullScreenEnabled = false
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
                    .background(Color.fromHex("#F2F2F2"))
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .clickable {
                            NavigationProvider.navController.navigate(
                                Routes.ShopDetails(
                                    shopVideo.shopsMaster.shopId ?: "",
                                    userId
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
                        painter = superShopRes,
                        contentDescription = "Save Video",
                    )
                }
            }
        }
    }
}