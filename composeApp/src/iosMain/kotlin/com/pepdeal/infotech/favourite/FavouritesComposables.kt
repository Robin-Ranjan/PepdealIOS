package com.pepdeal.infotech.favourite

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.FavProductWithImages
import com.pepdeal.infotech.ProductWithImages
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.red_heart


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FavoriteProductScreen(viewModal: FavoriteProductViewModal = ViewModals.favoriteProductViewModal) {

    val favProductList by viewModal.favoriteProduct.map { list ->
        list.sortedByDescending { it.createdAt.toLongOrNull() ?: 0L }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()
    val columnState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModal.getAllFavoriteProduct("-OIyeU1oyShOcB8r4-_8")
    }

    // Scroll to top when the list updates
    LaunchedEffect(favProductList) {
        if (favProductList.isNotEmpty()) {
            scope.launch {
                columnState.animateScrollToItem(0)
            }
        }
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
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            state = columnState
                        )
                        {
                            items(items = favProductList,
                                key = { it.product.productId }) { favProduct ->
                                var isVisible by remember { mutableStateOf(true) }
                                AnimatedContent(
                                    targetState = isVisible,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) { visible ->
                                    if (visible) {
                                        FavoriteProductCard(product = favProduct) { productId ->
                                            scope.launch {
                                                isVisible = false
                                                viewModal.removeFavItem(
                                                    "-OIyeU1oyShOcB8r4-_8",
                                                    productId
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
        }
    }
}

@Composable
fun FavoriteProductCard(
    product: FavProductWithImages,
    onDeleteClick: (String) -> Unit
) {
    val productDetails = product.product
    val productImage = product.images[0].productImages

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                CoilImage(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(width = 1.dp, color = Color.Gray),
                    imageModel = { productImage },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Column for Product Details
                Column(
                    modifier = Modifier
                        .weight(1f) // Takes up all available space
                        .padding(end = 40.dp) // Ensures icon does not overlap text
                ) {
                    Text(
                        text = productDetails.productName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2, // Allows wrapping to second line if needed
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    if (productDetails.onCall == "1") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = productDetails.mrp.toRupee(),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = productDetails.sellingPrice.toRupee(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (productDetails.discountMrp.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${productDetails.discountMrp}% off",
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "On Call",
                            fontSize = 14.sp,
                            color = Color.Black,
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        Text(
                            text = "Date: ",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = Util.formatDateWithTimestamp(product.createdAt),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Delete Icon (Top-Right Corner)
            IconButton(
                onClick = { onDeleteClick(productDetails.productId) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.red_heart),
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}


