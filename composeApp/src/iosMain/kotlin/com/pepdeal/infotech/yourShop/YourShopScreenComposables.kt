package com.pepdeal.infotech.yourShop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.shop.shopDetails.ShopProductCard
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.super_shop_logo

@Composable
fun YourShopScreen(
    shopId: String,
    viewModal: YourShopViewModal = ViewModals.yourShopViewModal
) {
//    val datastore = DataStore.dataStore
//    val currentUserId by datastore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
//        .collectAsState(initial = "-1")

    val shopDetails by viewModal.shopDetails.collectAsStateWithLifecycle()
    val shopLoading by viewModal.shopLoading.collectAsStateWithLifecycle()
    val shopProducts by viewModal.shopProduct.collectAsStateWithLifecycle()
    val shopServices by viewModal.shopServices.collectAsStateWithLifecycle()

    LaunchedEffect(shopId) {
        if (shopProducts.isEmpty()) {
            viewModal.fetchShopDetails(shopId)
            viewModal.fetchShopProducts(shopId)
            viewModal.fetchShopServices(shopId)
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

                when {
                    shopLoading -> {
                        CircularProgressIndicator(
                            color = Color.Yellow,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
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
                                    modifier = Modifier.height(30.dp).width(40.dp)
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

                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.super_shop_logo),
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
                            ShopServicesRow(shopServices)

                            // Product Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(items = shopProducts,
                                    key = { it.product.productId }) { product ->
                                    val heartIcon = Res.drawable.black_heart
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

                                            },
                                            onProductClicked = { productId ->
                                                NavigationProvider.navController.navigate(
                                                    Routes.ProductDetailsPage(
                                                        productId
                                                    )
                                                )
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
}

@Composable
fun ShopServicesRow(shopServices: ShopStatusMaster?) {
    val servicesMap = mapOf(
        "Cash on Delivery" to shopServices?.cashOnDelivery,
        "Door Step Service" to shopServices?.doorStep,
        "Home Delivery" to shopServices?.homeDelivery,
        "Live Demo" to shopServices?.liveDemo,
        "Offers" to shopServices?.offers,
        "Bargain" to shopServices?.bargain
    )

    val activeServices = servicesMap.filterValues { it == "0" }.keys.toList()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(activeServices) { service ->
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(5.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                    .wrapContentSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = service,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
