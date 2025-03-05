package com.pepdeal.infotech

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.banner.BannerMaster
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform


@Composable
fun BannerCarouselWidget(
    banners: List<BannerMaster>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = {
        banners.size
    })

    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            // Delay of 1 second before auto-swiping
            delay(3500)

            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(page = nextPage,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 8.dp,
            verticalAlignment = Alignment.Top,
        ) { page ->
            BannerWidget(
                imageUrl = banners[page].bannerImage,
                contentDescription = banners[page].bannerDescription
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}


@Composable
fun BannerWidget(
    imageUrl: String,
    contentDescription: String
) {
    CoilImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp)),
        imageModel = { imageUrl },
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillBounds,
            alignment = Alignment.Center
        ),
        previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
    )
}