package com.pepdeal.infotech.shop.shopDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import com.pepdeal.infotech.util.ViewModals
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.red_heart
import pepdealios.composeapp.generated.resources.super_shop_logo
import pepdealios.composeapp.generated.resources.super_shop_positive

@Composable
fun ShopDetailsWithProductPage(
    shopId: String,
    userId: String,
    viewModal: ShopDetailsViewModal = ViewModals.shopDetailsViewModal
) {
    val datastore = DataStore.dataStore
    val currentUserId by datastore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")

    val shopDetails by viewModal.shopDetails.collectAsStateWithLifecycle()
    val shopLoading by viewModal.shopLoading.collectAsStateWithLifecycle()
    val shopProducts by viewModal.shopProduct.collectAsStateWithLifecycle()
    val isSuperShop by viewModal.isSuperShop.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    // Track favorite states
    val favoriteStates = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(shopId) {
        scope.launch {
            viewModal.fetchShopDetails(shopId)
            viewModal.fetchShopProducts(shopId)
            viewModal.checkSuperShopExist(userId, shopId)
        }
    }

    DisposableEffect(shopDetails) {
        if (shopDetails.bgColourId != null) {
            Util.setStatusBarColor(
                shopDetails.bgColourId!!,
                isDark = false
            )
        }
        onDispose {
            Util.setStatusBarColor("#FFFFFF", isDark = true) // Reset to white with dark text
        }
    }

    MaterialTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (shopLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center // This centers the progress bar
                    ) {
                        CircularProgressIndicator(color = Color.Yellow)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {

                        // Title Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.fromHex(shopDetails.bgColourId ?: ""))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    viewModal.reset()
                                    NavigationProvider.navController.popBackStack()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }

                            Text(
                                text = shopDetails.shopName?.uppercase() ?: "",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 3.dp),
                                style = TextStyle(
                                    color = Color.fromHex(shopDetails.fontColourId),
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    fontFamily = FontFamily(
                                        Font(
                                            getFontResourceByName(
                                                shopDetails.fontStyleId ?: ""
                                            ) ?: Res.font.manrope_bold
                                        )
                                    ),
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(
                                onClick = { Util.openDialer(shopDetails.shopMobileNo ?: "") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color.Black
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModal.toggleSuperShop(userId, shopId, isSuperShop)
                                }
                            ) {
                                Icon(
                                    painter = if (isSuperShop) painterResource(Res.drawable.super_shop_positive) else painterResource(
                                        Res.drawable.super_shop_logo
                                    ),
                                    contentDescription = "Add to Super Shop"
                                )
                            }
                        }

                        // Shop Details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.fromHex(shopDetails.bgColourId ?: ""))
                                .padding(2.dp)
                        ) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(24.dp).padding(horizontal = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location"
                                )
                            }
                            Text(
                                text = shopDetails.shopAddress ?: "",
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Services (Horizontal Scroll)
//        LazyRow(
//            modifier = Modifier.fillMaxWidth(),
//            contentPadding = PaddingValues(horizontal = 8.dp)
//        ) {
//            items(services) { service ->
//                Card(
//                    shape = CircleShape,
//                    elevation = 5.dp,
//                    modifier = Modifier
//                        .padding(5.dp)
//                        .size(80.dp)
//                ) {
//                    Box(
//                        contentAlignment = Alignment.Center,
//                        modifier = Modifier.fillMaxSize()
//                    ) {
//                        Text(
//                            text = service,
//                            fontSize = 10.sp,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//            }
//        }

                        // Product Grid
//            if (!shopLoading) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = shopProducts,
                                key = { it.product.productId }) { product ->

                                // Determine the heart icon state
                                val isFavorite = favoriteStates[product.product.productId] ?: false
                                val heartIcon =
                                    if (isFavorite) Res.drawable.red_heart else Res.drawable.black_heart

                                // Check favorite status when the product is displayed
                                LaunchedEffect(product.product.productId) {
                                    if(currentUserId!="-1"){
                                        viewModal.checkFavoriteExists(
                                            userId = currentUserId,
                                            product.product.productId
                                        ) { exists ->
                                            favoriteStates[product.product.productId] = exists
                                        }
                                    }
                                }

                                // Shop Card
                                AnimatedVisibility(
                                    visible = true, // Replace with your condition if necessary
                                    enter = fadeIn(tween(durationMillis = 300)) + slideInVertically(
                                        initialOffsetY = { it }),
                                    exit = fadeOut(tween(durationMillis = 300)) + slideOutVertically(
                                        targetOffsetY = { it })
                                ) {
                                    ShopProductCard(product,
                                        painterResource(heartIcon),
                                        onLikeClicked = {
                                            val newFavoriteState = !isFavorite
                                            favoriteStates[product.product.productId] =
                                                newFavoriteState

                                            // Call ViewModel to handle like/unlike logic
                                            scope.launch {
                                                if(currentUserId!="-1"){
                                                    viewModal.toggleFavoriteStatus(
                                                        userId = currentUserId,
                                                        product.product.productId,
                                                        newFavoriteState
                                                    )
                                                }
                                            }
                                        })
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
fun ShopProductCard(
    shopItems: ProductWithImages,
    heartRes: Painter,
    onLikeClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(3.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(2.dp),
                    elevation = CardDefaults.elevatedCardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        imageModel = { shopItems.images[0].productImages },
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

                IconButton(
                    onClick = {
                        onLikeClicked()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        painter = heartRes,
                        contentDescription = "Like",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified,
                    )
                }
            }

            Text(
                text = shopItems.product.productName,
                fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth()
            )

            if (shopItems.product.onCall == "1") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = shopItems.product.sellingPrice.toTwoDecimalPlaces(),
                        fontFamily = FontFamily(Font(Res.font.manrope_light)),
                        fontSize = 11.sp,
                        lineHeight = 10.sp,
                        color = Color.Black
                    )

                    Text(
                        text = shopItems.product.mrp.toTwoDecimalPlaces(),
                        fontFamily = FontFamily(Font(Res.font.manrope_light)), fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )

                    Text(
                        text = shopItems.product.discountMrp.toDiscountFormat(),
                        fontFamily = FontFamily(Font(Res.font.manrope_light)), fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontFamily = FontFamily(Font(Res.font.manrope_light)), fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }
}