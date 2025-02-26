package com.pepdeal.infotech.categoriesProduct

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductCard
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kottieAnimation.KottieAnimation
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.red_heart
import utils.KottieConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun CategoryWiseProductScreen(
    subCategoryName: String,
    viewModal: CategoryWiseProductViewModal = ViewModals.categoryWiseProductViewModal
) {

    val dataStore = DataStore.dataStore
    val currentUserId by dataStore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")
    val listState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var filteredProducts by remember { mutableStateOf<List<ShopItems>>(emptyList()) }
    // Track favorite states
    val favoriteStates = remember { mutableStateMapOf<String, Boolean>() }

    //observer
    val productList by viewModal.products.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()
    val isEmpty by viewModal.isEmpty.collectAsStateWithLifecycle()

    // lottie Animation
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


    // filtering
    val displayedProductList = remember(searchQuery, productList) {
        if (searchQuery.isNotEmpty()) filteredProducts else productList
    }

    LaunchedEffect(subCategoryName) {
        viewModal.fetchCategoryProducts(subCategoryName)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = subCategoryName.toNameFormat(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModal.reset()
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
                        containerColor = Color.White, // Background color
                        titleContentColor = Color.Black, // Title color
                        navigationIconContentColor = Color.Black, // Back button color
                        actionIconContentColor = Color.Unspecified
                    ),
                    modifier = Modifier.shadow(4.dp)
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBar, snackbar = { data ->
                    Snackbar(content = { Text(text = data.visuals.message) })
                })
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    isEmpty -> {
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
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2), // 2 columns
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp), // Space between columns
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Space between rows
                            state = listState
                        ) {
                            items(items = displayedProductList,
                                key = { it.productId }) { product ->
                                // Determine the heart icon state
                                val isFavorite = favoriteStates[product.productId] ?: false
                                val heartIcon =
                                    if (isFavorite && currentUserId !== "-1") Res.drawable.red_heart else Res.drawable.black_heart

                                // Check favorite status when the product is displayed
                                LaunchedEffect(product.productId) {
                                    if (currentUserId != "-1") {
                                        viewModal.checkFavoriteExists(
                                            currentUserId,
                                            product.productId
                                        ) { exists ->
                                            favoriteStates[product.productId] = exists
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
                                    ProductCard(
                                        shopItems = product,
                                        heartRes = painterResource(heartIcon),
                                        onLikeClicked = {
                                            if (currentUserId != "-1") {
                                                val newFavoriteState = !isFavorite
                                                favoriteStates[product.productId] = newFavoriteState
                                                // Call ViewModel to handle like/unlike logic
                                                coroutineScope.launch {
                                                    viewModal.toggleFavoriteStatus(
                                                        userId = currentUserId,
                                                        product.productId,
                                                        newFavoriteState
                                                    )
                                                }
                                            }
                                        },
                                        onProductClicked = {
                                            NavigationProvider.navController.navigate(Routes.ProductDetailsPage(it))
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