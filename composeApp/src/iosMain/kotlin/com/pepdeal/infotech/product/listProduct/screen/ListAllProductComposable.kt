package com.pepdeal.infotech.product.listProduct.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.listProduct.viewModel.ListAllProductViewModal
import com.pepdeal.infotech.util.NavigationProvider.navController
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import utils.KottieConstants

@Composable
fun ListAllProductScreenRoot(viewModel: ListAllProductViewModal = koinViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackBarMessage) {
        uiState.snackBarMessage?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
                viewModel.onAction(ListAllProductViewModal.ListAllProductAction.OnClearSnackBar)
            }
        }
    }
    ListAllProductScreen(
        uiState,
        snackBarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAllProductScreen(
    uiState: ListAllProductViewModal.ListAllProductState,
    snackBarHostState: SnackbarHostState,
    onAction: (ListAllProductViewModal.ListAllProductAction) -> Unit
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
                // Top App Bar with Back Button
                TopAppBar(
                    title = {
                        Text(
                            "List Product",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold))
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
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
                    expandedHeight = 50.sdp
                )
            },
            containerColor = Color.White
        ) { paddingValue ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValue)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                        LazyColumn {
                            items(
                                items = uiState.productWithImages,
                                key = { it.product.productId }) { products ->
                                var isLive by remember { mutableStateOf(products.product.productActive == "0") }
                                ListProductCard(
                                    products,
                                    isLive = isLive,
                                    onSwitchChanged = { newState ->
                                        isLive = newState
                                        onAction(
                                            ListAllProductViewModal.ListAllProductAction.UpdateProductStatusByShopOwner(
                                                products.product.productId,
                                                if (newState) "0" else "1"
                                            )
                                        )
                                    },
                                    onUpdateClick = {
                                        navController.navigate(Routes.UpdateProductPage(products.product.productId))
                                    }, onRemoveClick = {

                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

