package com.pepdeal.infotech.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.ShopCardView
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo
import pepdealios.composeapp.generated.resources.pepdeal_logo_new
import pepdealios.composeapp.generated.resources.place_holder
import pepdealios.composeapp.generated.resources.red_heart

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModal = ViewModals.productViewModal) {

    // dataStore
    val dataStore = DataStore.dataStore
    val currentUserId by dataStore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")

    // Observables
    val productNewList by viewModel.products.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.searchedProducts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val isSearchLoading by viewModel.isSearchLoading.collectAsStateWithLifecycle()

    // Variables
    val listState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchActive by remember { mutableStateOf(false) }

    // Track favorite states
    val favoriteStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(Unit) {
        if (productNewList.isEmpty()) {
            coroutineScope.launch {
                viewModel.fetchItemsPage()
            }
        }
    }

    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery.trim() }
            .debounce(1000)
            .distinctUntilChanged()
            .collectLatest { debouncedQuery ->
                // Call your viewModel function with the debounced search query
                viewModel.fetchSearchedItemsPage(debouncedQuery)
            }
    }

    // Observe scroll position to load more when reaching near the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 10 && !viewModel.isLoading.value && searchQuery.isEmpty()) {
                    coroutineScope.launch {
                        println("loading more ")
                        viewModel.fetchItemsPage()
                    }
                }
            }
    }

    MaterialTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 3.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            keyboardController?.hide()
                        })
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (!isSearchActive) {
                        // App Logo
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.pepdeal_logo_new),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(28.dp)
                                    .padding(start = 5.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }

                    SearchBar(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        colors = SearchBarColors(
                            containerColor = Color.White,
                            dividerColor = Color.Gray
                        ),
                        shape = RectangleShape,
                        shadowElevation = SearchBarDefaults.TonalElevation,
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = { /* Implement search logic here */ },
                                expanded = isSearchActive,
                                onExpandedChange = { isSearchActive = it },
                                modifier = Modifier.fillMaxWidth().padding(0.dp),
                                placeholder = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .wrapContentHeight(Alignment.CenterVertically)
                                    ) {
                                        Text(
                                            "Search Product",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                },
                                leadingIcon = {
                                    if (isSearchActive) {
                                        IconButton(onClick = {
                                            isSearchActive = false
                                            searchQuery = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Icon"
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Search"
                                            )
                                        }
                                    }
                                },
//                            colors = TextFieldColors()
                            )
                        },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                    ) {
                        when {
                            isSearchLoading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f), // Keeps it centered properly
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.Blue)
                                }
                            }

                            else -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize().padding(5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    if (filteredProducts.isEmpty()) {
                                        item {
                                            Text(
                                                text = "No shops found",
                                                modifier = Modifier.padding(16.dp),
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        items(
                                            filteredProducts,
                                            key = { it.productId }) { product ->
                                            val isFavorite =
                                                favoriteStates[product.productId] ?: false
                                            val heartIcon =
                                                if (isFavorite && currentUserId != "-1") {
                                                    Res.drawable.red_heart
                                                } else {
                                                    Res.drawable.black_heart
                                                }

                                            // Load Favorite Status for Logged-In Users
                                            LaunchedEffect(product.productId) {
                                                if (currentUserId != "-1" && !favoriteStates.containsKey(
                                                        product.productId
                                                    )
                                                ) {
                                                    viewModel.checkFavoriteExists(
                                                        currentUserId,
                                                        product.productId
                                                    ) { exists ->
                                                        favoriteStates[product.productId] = exists
                                                    }
                                                }
                                            }

                                            // Product Card with Animation
                                            AnimatedVisibility(
                                                visible = true,
                                                enter = fadeIn(tween(300)) + slideInVertically(
                                                    initialOffsetY = { it }),
                                                exit = fadeOut(tween(300)) + slideOutVertically(
                                                    targetOffsetY = { it })
                                            ) {
                                                ProductCard(
                                                    shopItems = product,
                                                    heartRes = painterResource(heartIcon),
                                                    onLikeClicked = {
                                                        if (currentUserId == "-1") {
                                                            Util.showToast("Login Please")
                                                        } else {
                                                            val newFavoriteState = !isFavorite
                                                            favoriteStates[product.productId] =
                                                                newFavoriteState
                                                            coroutineScope.launch {
                                                                viewModel.toggleFavoriteStatus(
                                                                    userId = currentUserId,
                                                                    productId = product.productId,
                                                                    isFavorite = newFavoriteState
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onProductClicked = {
                                                        NavigationProvider.navController.navigate(
                                                            Routes.ProductDetailsPage(it)
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

                    // Product Grid
                    when {
                        isLoading && productNewList.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.Blue)
                            }
                        }

                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize().padding(5.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                state = listState
                            ) {
                                items(productNewList, key = { it.productId }) { product ->
                                    val isFavorite = favoriteStates[product.productId] ?: false
                                    val heartIcon = if (isFavorite && currentUserId != "-1") {
                                        Res.drawable.red_heart
                                    } else {
                                        Res.drawable.black_heart
                                    }

                                    // Load Favorite Status for Logged-In Users
                                    LaunchedEffect(product.productId) {
                                        if (currentUserId != "-1" && !favoriteStates.containsKey(
                                                product.productId
                                            )
                                        ) {
                                            viewModel.checkFavoriteExists(
                                                currentUserId,
                                                product.productId
                                            ) { exists ->
                                                favoriteStates[product.productId] = exists
                                            }
                                        }
                                    }

                                    // Product Card with Animation
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(tween(300)) + slideInVertically(
                                            initialOffsetY = { it }),
                                        exit = fadeOut(tween(300)) + slideOutVertically(
                                            targetOffsetY = { it })
                                    ) {
                                        ProductCard(
                                            shopItems = product,
                                            heartRes = painterResource(heartIcon),
                                            onLikeClicked = {
                                                if (currentUserId == "-1") {
                                                    Util.showToast("Login Please")
                                                } else {
                                                    val newFavoriteState = !isFavorite
                                                    favoriteStates[product.productId] =
                                                        newFavoriteState
                                                    coroutineScope.launch {
                                                        viewModel.toggleFavoriteStatus(
                                                            userId = currentUserId,
                                                            productId = product.productId,
                                                            isFavorite = newFavoriteState
                                                        )
                                                    }
                                                }
                                            },
                                            onProductClicked = {
                                                NavigationProvider.navController.navigate(
                                                    Routes.ProductDetailsPage(it)
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
//    }
}

@Composable
fun ProductCard(
    shopItems: ShopItems,
    heartRes: Painter,
    onLikeClicked: () -> Unit,
    onProductClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .clickable { onProductClicked(shopItems.productId) },
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
                        imageModel = { shopItems.image },
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
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop,
                            )
                        },
                        failure = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                }

                // Discount Badge (Top-Left)
                if (shopItems.discountMrp.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.Yellow, shape = RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${shopItems.discountMrp.toDiscountFormat()} OFF",
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
                        painter = heartRes,
                        contentDescription = "Like",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified,
                    )
                }
            }

            Text(
                text = shopItems.productName,
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

            if (shopItems.onCall == "1") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = shopItems.sellingPrice.toTwoDecimalPlaces(),
                        fontFamily = FontFamily(Font(Res.font.manrope_light)),
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = Color.Black
                    )

                    Text(
                        text = shopItems.mrp.toTwoDecimalPlaces(),
                        fontFamily = FontFamily(Font(Res.font.manrope_light)), fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )

//                    Text(
//                        text = shopItems.discountMrp.toDiscountFormat(),
//                        fontFamily = FontFamily(Font(Res.font.manrope_light)), fontSize = 10.sp,
//                        lineHeight = 10.sp,
//                        color = Color.Red,
//                        modifier = Modifier.padding(start = 3.dp)
//                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontFamily = FontFamily(Font(Res.font.manrope_light)),
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}


@Composable
fun SearchView(label: String, searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp), // Rounded corners
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(Color.White),
        border = BorderStroke(0.5.dp, Color.Black) // Stroke color and width
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp) // Padding for the content inside the card
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black,
                modifier = Modifier.align(Alignment.CenterVertically)
                    .padding(start = 3.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            TextField(
                value = searchQuery,
                onValueChange = { newQuery -> onSearchQueryChanged(newQuery) },
                label = {
                    if (!isFocused && searchQuery.isEmpty()) {
                        Text(label, fontSize = 14.sp, color = Color.Gray, lineHeight = 14.sp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .focusRequester(focusRequester) // Attach focusRequester to the TextField
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused // Track focus state
                    },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    // Customize TextField colors
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.LightGray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    errorContainerColor = Color.Red.copy(alpha = 0.1f),
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent, // No underline
                    unfocusedIndicatorColor = Color.Transparent, // No underline
                    disabledIndicatorColor = Color.Transparent, // No underline
                    errorIndicatorColor = Color.Red, // Red underline for error state
                    focusedLabelColor = Color.Gray, // Label color when focused
                    unfocusedLabelColor = Color.Gray, // Label color when unfocused
                    errorLabelColor = Color.Red, // Label color for error state
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = Color.Gray
                            )
                        }
                    }
                },
            )
        }
    }
}
