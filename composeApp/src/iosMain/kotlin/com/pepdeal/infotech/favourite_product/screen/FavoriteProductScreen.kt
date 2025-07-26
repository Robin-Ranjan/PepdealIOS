package com.pepdeal.infotech.favourite_product.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.favourite_product.screen.component.FavoriteProductCard
import com.pepdeal.infotech.favourite_product.viewModel.FavoriteProductViewModal
import com.pepdeal.infotech.favourite_product.viewModel.ProductFavAction
import com.pepdeal.infotech.favourite_product.viewModel.ProductFavUiState
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import utils.KottieConstants

@Composable
fun FavoriteProductScreenRoot(viewModal: FavoriteProductViewModal = koinViewModel()) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
                viewModal.onAction(ProductFavAction.OnClearSnackBar)
            }
        }
    }

    FavoriteProductScreen(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onAction = viewModal::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteProductScreen(
    uiState: ProductFavUiState,
    snackBarHostState: SnackbarHostState,
    onAction: (ProductFavAction) -> Unit
) {
    // variables
    val columnState = rememberLazyListState()
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
                            text = "Favourite Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
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
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    ),
                    modifier = Modifier.shadow(4.dp)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues) // Apply padding from Scaffold
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.isEmpty -> {
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
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            state = columnState
                        ) {
                            items(
                                items = uiState.favoriteProduct,
                                key = { it.product.productId }) { favProduct ->
                                var isVisible by remember { mutableStateOf(true) }
                                AnimatedContent(
                                    targetState = isVisible,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith
                                                fadeOut(animationSpec = tween(300))
                                    }
                                ) { visible ->
                                    if (visible) {
                                        FavoriteProductCard(
                                            product = favProduct,
                                            onDeleteClick = { productId ->
                                                isVisible = false
                                                onAction(ProductFavAction.OnRemoveFav(productId))
                                            }, onFavClick = { productId ->
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


