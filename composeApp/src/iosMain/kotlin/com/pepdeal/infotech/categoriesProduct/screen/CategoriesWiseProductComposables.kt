package com.pepdeal.infotech.categoriesProduct.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.categoriesProduct.viewModel.CategoryWiseProductViewModal
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.manrope_semibold
import pepdealios.composeapp.generated.resources.place_holder
import utils.KottieConstants

@Composable
fun CategoryWiseProductScreenRoot(viewModal: CategoryWiseProductViewModal = koinViewModel()) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
                viewModal.onAction(CategoryWiseProductViewModal.Action.OnClearSnackBar)
            }
        }
    }

    CategoryWiseProductScreen(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onAction = viewModal::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryWiseProductScreen(
    uiState: CategoryWiseProductViewModal.CategoryWiseProductState,
    snackBarHostState: SnackbarHostState,
    onAction: (CategoryWiseProductViewModal.Action) -> Unit,
) {
    var animation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            animation = Res.readBytes("files/empty_list.json").decodeToString()
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


    MaterialTheme {
        Scaffold(
            snackbarHost = {
                CustomSnackBarHost(
                    snackBarHostState,
                    currentMessage = uiState.snackBarMessage
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.subCategoryName.toNameFormat(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            NavigationProvider.navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                        actionIconContentColor = Color.Unspecified
                    ),
                    modifier = Modifier.shadow(4.dp)
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.isEmpty -> {
                        KottieAnimation(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp),
                            composition = composition,
                            progress = { animationState.progress }
                        )
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyGridState()
                        ) {
                            items(
                                items = uiState.data,
                                key = { it.product.productId }) { item ->

                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(durationMillis = 300)) + slideInVertically(
                                        initialOffsetY = { it }),
                                    exit = fadeOut(tween(durationMillis = 300)) + slideOutVertically(
                                        targetOffsetY = { it })
                                ) {
                                    CategoriesWiseProductCard(
                                        categoryProduct = item,
                                        onLikeClicked = {
                                            onAction(
                                                CategoryWiseProductViewModal.Action.OnToggleFavoriteStatus(
                                                    productId = item.product.productId
                                                )
                                            )
                                        },
                                        onProductClicked = {
                                            NavigationProvider.navController.navigate(
                                                Routes.ProductDetailsPage(
                                                    it
                                                )
                                            )
                                        }
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

@Composable
fun CategoriesWiseProductCard(
    categoryProduct: CategoryWiseProductViewModal.CategoryWiseProductModel,
    onLikeClicked: () -> Unit,
    onProductClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .clickable { onProductClicked(categoryProduct.product.productId) },
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
                        imageModel = { categoryProduct.product.image },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                setToSaturation(1f)
                            })
                        ),
                        previewPlaceholder = painterResource(Res.drawable.place_holder),
                        loading = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder),
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop,
                            )
                        },
                        failure = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder),
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                }

                if (categoryProduct.product.discountMrp.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                Color(0xFFFF9800).copy(alpha = 0.7f),
                                shape = RoundedCornerShape(bottomEnd = 8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = categoryProduct.product.discountMrp.toDiscountFormat(),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                        imageVector = if (categoryProduct.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(30.dp),
                        tint = if (categoryProduct.isFavorite) Color.Red else Color.Black,
                    )
                }
            }

            Text(
                text = categoryProduct.product.productName.toNameFormat(),
                fontSize = 13.sp,
                lineHeight = 13.sp,
                color = Color.Black,
                maxLines = 1,
                fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth()
            )

            if (categoryProduct.product.onCall == "1") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = categoryProduct.product.sellingPrice.toTwoDecimalPlaces().toRupee(),
                        fontFamily = FontFamily(Font(Res.font.manrope_semibold)),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = Color.Black
                    )

                    Text(
                        text = categoryProduct.product.mrp.toTwoDecimalPlaces().toRupee(),
                        fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(horizontal = 5.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                    color = Color.Red,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}
