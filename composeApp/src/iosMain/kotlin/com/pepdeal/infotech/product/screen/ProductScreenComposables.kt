package com.pepdeal.infotech.product.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.core.basic_ui.AppSearchBar
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductViewModel
import com.pepdeal.infotech.product.screen.component.ProductCardNew
import com.pepdeal.infotech.shop.BackGroundColor
import com.pepdeal.infotech.shop.screen.component.SearchTagItemCard
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import dev.jordond.compass.geocoder.MobileGeocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo

@Composable
fun ProductScreenRoot(viewModel: ProductViewModel = koinViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val searchTags by viewModel.searchTags.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it.message.isNotBlank()) {
                snackbarHostState.showSnackbar(it.message)
                viewModel.onAction(ProductViewModel.Action.OnResetMessage)
            }
        }
    }

    ProductScreen(
        uiState,
        searchTags,
        onAction = viewModel::onAction,
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        newAddress = uiState.address?.address
    )
}

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    uiState: ProductViewModel.UiState,
    searchTags: ProductViewModel.SearchProductTags,
    onAction: (ProductViewModel.Action) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: ProductViewModel,
    newAddress: String? = null,
) {
    // Variables
    var locationName: String? by rememberSaveable { mutableStateOf(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    val geoLocation = rememberSaveable { Geolocator.mobile() }

    LaunchedEffect(Unit) {
        if (locationName == null) {
            when (val result = geoLocation.current()) {
                is GeolocatorResult.Success -> {
                    val coordinates = result.data.coordinates
                    val lat = coordinates.latitude
                    val lng = coordinates.longitude

                    locationName =
                        MobileGeocoder().placeOrNull(result.data.coordinates)?.subLocality

                    if (uiState.products.isEmpty()) {
                        viewModel.fetchItems(
                            userLng = 77.2779323,
                            userLat = 28.6465035
                        )
                    }
                }

                is GeolocatorResult.Error -> when (result) {
                    is GeolocatorResult.NotSupported -> println("LOCATION ERROR: ${result.message}")
                    is GeolocatorResult.NotFound -> println("LOCATION ERROR: ${result.message}")
                    is GeolocatorResult.PermissionError -> println("LOCATION ERROR: ${result.message}")
                    is GeolocatorResult.GeolocationFailed -> println("LOCATION ERROR: ${result.message}")
                    else -> println("LOCATION ERROR: ${result.message}")
                }
            }
        }
    }
    val displayedLocation = newAddress ?: locationName

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                CustomSnackBarHost(
                    hostState = snackbarHostState,
                    currentMessage = uiState.message
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 3.dp)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            keyboardController?.hide()
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!isSearchActive) {
                        // App Logo
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.pepdeal_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(28.dp)
                                    .padding(start = 5.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp).clickable {
                                viewModel.onAction(ProductViewModel.Action.OnLocationClick)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = displayedLocation?.toNameFormat() ?: "Fetching...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    val suggestions = listOf("Food 🍔", "Fashion 👗", "Hotels 🏨", "Electronics 📱")
                    AppSearchBar(
                        searchQuery = searchTags.query,
                        onSearchQueryChange = {
                            onAction(ProductViewModel.Action.OnSearchQueryChange(it))
                        },
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it },
                        isSearchLoading = searchTags.isLoading,
                        suggestionsList = suggestions,
                        onSearchTriggered = {
                            NavigationProvider.navController.navigate(
                                Routes.ProductSearchRoute(
                                    it,
                                    uiState.user?.userId
                                )
                            )
                        },
                        onClearClick = { onAction(ProductViewModel.Action.OnSearchQueryChange("")) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent()
                                            keyboardController?.hide()
                                        }
                                    }
                                }
                        ) {
                            when {
                                searchTags.isLoading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                searchTags.isEmpty -> {
                                    Text(
                                        text = "No Tags found",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = Color.Gray
                                    )
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 5.dp)
                                            .background(BackGroundColor)
                                    ) {
                                        items(searchTags.topSearchTags) {
                                            SearchTagItemCard(it) { queryTag ->
                                                NavigationProvider.navController.navigate(
                                                    Routes.ProductSearchRoute(
                                                        queryTag,
                                                        uiState.user?.userId
                                                    )
                                                )
                                            }
                                        }

                                        if (searchTags.topProductNames.isNotEmpty()) {
                                            item {
                                                Text(
                                                    text = "Search By Product Name",
                                                    fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(
                                                        top = 8.dp,
                                                        bottom = 4.dp,
                                                        start = 12.dp
                                                    ),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        items(searchTags.topProductNames) {
                                            SearchTagItemCard(it) { queryTag ->
                                                NavigationProvider.navController.navigate(
                                                    Routes.ProductSearchRoute(
                                                        it,
                                                        uiState.user?.userId
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.Blue)
                            }
                        }

                        uiState.isEmpty -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PepDeal isn’t available in your location just yet.")
                            }
                        }

                        else -> {
                            val product = uiState.products
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(5.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(product, key = { it.shopItem.productId }) { product ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(tween(300)) + slideInVertically(
                                            initialOffsetY = { it }),
                                        exit = fadeOut(tween(300)) + slideOutVertically(
                                            targetOffsetY = { it })
                                    ) {
                                        ProductCardNew(
                                            shopItems = product.shopItem,
                                            isFavorite = product.isFavourite,
                                            onFavoriteClick = {
                                                println("click happen ")
                                                onAction(
                                                    ProductViewModel.Action.OnFavClick(
                                                        product.shopItem
                                                    )
                                                )
                                            },
                                            onProductClicked = {
                                                onAction(
                                                    ProductViewModel.Action.OnClickProduct(
                                                        product.shopItem
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
}
