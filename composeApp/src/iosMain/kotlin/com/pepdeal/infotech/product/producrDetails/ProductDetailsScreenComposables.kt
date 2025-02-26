package com.pepdeal.infotech.product.producrDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.flow.map
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.baseline_person_24
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.manrope_regular
import pepdealios.composeapp.generated.resources.original
import pepdealios.composeapp.generated.resources.red_heart
import utils.KottieConstants

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModal: ProductDetailsViewModal = ViewModals.productDetailsViewModal
) {
    //dataStore
    val datastore = DataStore.dataStore
    val currentUserId by datastore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")

    // variables
    var heartRes by remember { mutableStateOf(Res.drawable.black_heart)  }
    var isFavorite by remember { mutableStateOf(false) }

    // observers
    val product by viewModal.product.collectAsStateWithLifecycle()
    val shop by viewModal.shop.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()

    // Animation
    var animation by remember { mutableStateOf("") }
    val composition = rememberKottieComposition(
        spec = KottieCompositionSpec.JsonString(animation)
    )

    val animationState by animateKottieCompositionAsState(
        composition = composition,
        iterations = KottieConstants.IterateForever,
        isPlaying = true
    )

    // launchEffects
    LaunchedEffect(productId) {
        viewModal.getTheProductDetails(productId)
    }

    LaunchedEffect(product) {
        product?.product?.let {
            viewModal.getTheShopDetails(it.shopId)
        }
    }

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/empty_list.json").decodeToString()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    LaunchedEffect(currentUserId ,productId) {
        if (currentUserId != "-1") {
            viewModal.checkFavoriteExists(currentUserId, productId) { isFav ->
                isFavorite = isFav
               heartRes = if(isFav) Res.drawable.red_heart else Res.drawable.black_heart
                println(isFav)
            }
        }

        println(currentUserId)
    }


    MaterialTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {

                    },
                    modifier = Modifier
                        .padding(16.dp),
                    shape = RoundedCornerShape(corner = CornerSize(8.dp)),
                    content = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Floating Button",
                            tint = Color.Black
                        )
                    },
                    containerColor = Color.White)
            },
            floatingActionButtonPosition = FabPosition.EndOverlay,
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {

                    when {
                        isLoading -> {
                            CircularProgressIndicator()
                        }

                        product != null -> {

                            LazyColumn {
                                item {
                                    ProductImagesCarouselWidget(
                                        productImages = product?.images ?: emptyList(),
                                        heartRes = heartRes,
                                        onBackClicked = {
                                            viewModal.reset()
                                            NavigationProvider.navController.popBackStack()
                                        },
                                        onLikeClick = {
                                            if(currentUserId!="-1"){
                                                viewModal.toggleFavoriteStatus(userId = currentUserId, productId = productId, isFavorite = !isFavorite)
                                                isFavorite = !isFavorite
                                                heartRes = if(isFavorite) Res.drawable.red_heart else Res.drawable.black_heart
                                            }
                                        }
                                    )
                                }

                                item {
                                    ProductInfoSection(product!!.product)
                                }
                                item {
                                    WarrantySection(product!!.product)
                                }

                                item {
                                    AdditionalDetailsSection(product!!.product)
                                }

                                item {
                                    shop?.let { ProductShopInfoSection(it) }
                                }
                            }
                        }

                        else -> {
                            // Lottie Animation for Empty State
                            KottieAnimation(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 50.dp),
                                composition = composition,
                                progress = { animationState.progress }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductShopInfoSection(shopMaster: ShopMaster) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Shop Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(
                Font(Res.font.manrope_bold)
            )
        )
        Text(
            text = shopMaster.shopName?.toNameFormat() ?: "",
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(Res.font.manrope_regular))
        )

        Text(
            text = shopMaster.shopMobileNo ?: "",
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(Res.font.manrope_regular))
        )
    }
}

@Composable
fun ProductInfoSection(product: ProductMaster) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = product.productName.toNameFormat(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            if(product.onCall == "1"){
                Text(text = product.discountMrp.toDiscountFormat(), fontSize = 16.sp, color = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.sellingPrice.toTwoDecimalPlaces().toRupee(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.mrp.toTwoDecimalPlaces().toRupee(),
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.LineThrough,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "On Call".toNameFormat(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun WarrantySection(product: ProductMaster) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = product.warranty, fontSize = 11.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.original),
                contentDescription = "Original",
//                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = product.brandId, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = product.categoryId.toNameFormat(), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AdditionalDetailsSection(product: ProductMaster) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Product Description",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(
                Font(Res.font.manrope_bold)
            )
        )
        Text(
            text = product.description.toNameFormat(),
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(Res.font.manrope_regular))
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Product Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = product.specification.toNameFormat(), fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Additional Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = product.description2.toNameFormat(), fontSize = 12.sp, color = Color.Gray)
    }
}


@Composable
fun ProductImagesCarouselWidget(
    productImages: List<ProductImageMaster>,
    modifier: Modifier = Modifier,
    heartRes: DrawableResource,
    onBackClicked: () -> Unit,
    onLikeClick:() -> Unit
) {
    val pagerState = rememberPagerState(pageCount = {
        productImages.size
    })

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
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
                    painter = painterResource(heartRes),
                    contentDescription = "Favorite",
                    tint = Color.Unspecified,
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
fun ProductImageWidget(
    imageUrl: String,
) {
    CoilImage(
        modifier = Modifier
            .fillMaxSize(),
        imageModel = { imageUrl },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        ),
        previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
    )
}