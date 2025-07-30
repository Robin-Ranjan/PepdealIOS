package com.pepdeal.infotech.product.producrDetails.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.producrDetails.ProductImageWidget
import kotlinx.coroutines.delay

@Composable
fun ProductImagesCarouselWidget(
    productImages: List<ProductImageMaster>,
    modifier: Modifier = Modifier,
    isfav: Boolean,
    onBackClicked: () -> Unit,
    onLikeClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = {
        productImages.size
    })

    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            // Delay of 1 second before auto-swiping
            delay(1500)

            val nextPage = (pagerState.currentPage + 1) % productImages.size
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        // Image Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 8.dp,
            verticalAlignment = Alignment.Top,
        ) { page ->
            ProductImageWidget(
                imageUrl = productImages[page].productImages
            )
        }

        // Overlay Box for Back & Favorite buttons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f) // Ensures buttons stay on top
        ) {
            // Back Button (Top-Start)
            IconButton(
                onClick = { onBackClicked() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp)
                )
            }

            // Favorite Button (Top-End)
            IconButton(
                onClick = { onLikeClick() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f)),
            ) {
                Icon(
                    imageVector = if (isfav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isfav) Color.Red else Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Page Indicators
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }
    }
}