package com.pepdeal.infotech.superShop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.ShopItemView
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.super_shop_positive
import utils.KottieConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun SuperShopScreen(userId: String, viewModal: SuperShopViewModal = ViewModals.superShopViewModal) {

    val superShops by viewModal.superShop.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle(initialValue = false)
    val isEmpty by viewModal.isLoading.collectAsStateWithLifecycle(initialValue = false)

    val columnState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    //Animation
    var animation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/empty_list.json").decodeToString()
            println("Animation JSON: $animation")
        } catch (e: Exception) {
            println(e.message)
        }
    }

    val composition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(animation)
    )

    val animationState by animateKottieCompositionAsState(
        composition = composition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        if (superShops.isEmpty()) {
            viewModal.fetchSuperShop(userId)
        }
    }

    // Outer CardView
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.x != 0f) Offset.Zero else super.onPreScroll(available, source)
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Super Shops",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModal.reset()
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
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    isEmpty -> {
                        println(isEmpty)
                        // Lottie Animation for Empty State
                        KottieAnimation(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp),
                            composition = composition,
                            progress = { animationState.progress }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                                .padding(5.dp)
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures { change, dragAmount ->

                                    }
                                },
                            state = columnState
                        ) {
                            items(superShops,
                                key = { it.shop.shopId!! }
                            ) { shop ->
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
                                        // Shop Card
                                        SuperShopCardView(shop, onDeleteClick = { shopId ->
                                            scope.launch {
                                                isVisible = false
                                                if (shopId != "-1") viewModal.removeSuperShop(
                                                    userId,
                                                    shopId
                                                )
                                            }
                                        },
                                            onShopClicked = {
                                                NavigationProvider.navController.navigate(
                                                    Routes.ShopDetails(
                                                        it,
                                                        userId
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
fun SuperShopCardView(
    superShopWithProduct: SuperShopsWithProduct,
    onDeleteClick: (String) -> Unit,
    onShopClicked: (String) -> Unit
) {
    val cardBackgroundColor = Color.fromHex(superShopWithProduct.shop.bgColourId ?: "#FFFFFF")
    val shopNameColor = Color.fromHex(superShopWithProduct.shop.fontColourId)
    val fontResource: FontResource =
        getFontResourceByName(superShopWithProduct.shop.fontStyleId ?: "") ?: Res.font.manrope_bold
    val customFont = FontFamily(Font(fontResource))

    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackgroundColor)
                    .clickable { superShopWithProduct.shop.shopId?.let { onShopClicked(it) } }
                    .padding(5.dp)
            ) {
                // Shop Name and Address
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        text = superShopWithProduct.shop.shopName.orEmpty(),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = customFont
                        ),
                        color = shopNameColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = superShopWithProduct.shop.shopAddress2 ?: "",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = shopNameColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Icons (Call & Delete)
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { Util.openDialer("") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.Black
                        )
                    }
                    IconButton(
                        onClick = { onDeleteClick(superShopWithProduct.shop.shopId ?: "-1") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.super_shop_positive),
                            contentDescription = "Delete",
                            tint = Color.Black
                        )
                    }
                }
            }

            // Products List (LazyRow)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(
                    items = superShopWithProduct.products,
                    key = { it.product.productId }
                ) { shopItem ->
                    ShopItemView(shopItem) {
                        NavigationProvider.navController.navigate(Routes.ProductDetailsPage(it))
                    }
                }
            }
        }
    }
}

