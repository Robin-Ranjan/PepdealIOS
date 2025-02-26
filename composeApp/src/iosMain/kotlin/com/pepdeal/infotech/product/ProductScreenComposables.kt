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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_light
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo
import pepdealios.composeapp.generated.resources.red_heart


@Composable
fun ProductScreen(viewModel: ProductViewModal = ViewModals.productViewModal) {

    val dataStore = DataStore.dataStore
    val currentUserId by dataStore.data.map { it[PreferencesKeys.USERID_KEY] ?: "-1" }
        .collectAsState(initial = "-1")
    val listState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val productNewList by viewModel.products.collectAsStateWithLifecycle()
    var filteredProducts by remember { mutableStateOf<List<ShopItems>>(emptyList()) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Track favorite states
    val favoriteStates = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) {
        if (productNewList.isEmpty()) {
            coroutineScope.launch {
                viewModel.fetchItemsPage()
            }
        }
    }

    val displayedProductList = remember(searchQuery, productNewList) {
        if (searchQuery.isNotEmpty()) filteredProducts else productNewList
    }

    // Observe search query and filter in the background
    LaunchedEffect(searchQuery, productNewList) {
        withContext(Dispatchers.Default) {
            val filtered = productNewList.filter { product ->
                // Split product's searchTags by commas
                product.searchTag.split(",").any { tag ->
                    // Check if any tag matches the searchQuery
                    tag.contains(searchQuery, ignoreCase = true)
                }
            }
            withContext(Dispatchers.Main) {
                filteredProducts = filtered
            }
        }
    }


    // Observe scroll position to load more when reaching near the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 5 && !viewModel.isLoading.value && searchQuery.isEmpty()) {
                    coroutineScope.launch {
                        viewModel.fetchItemsPage()
                    }
                }
            }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(5.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                }
        ) {

            Column {
                Image(
                    painter = painterResource(Res.drawable.pepdeal_logo),
                    contentDescription = "Your image description",
                    modifier = Modifier
                        .width(130.dp)
                        .height(28.dp)
                        .padding(start = 5.dp),
                    contentScale = ContentScale.Fit // Adjust based on your needs (e.g., FillBounds, Fit)
                )
                SearchView("Search Product", searchQuery) {
                    searchQuery = it
                }

                Text(text = productNewList.size.toString())
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
                            if (isFavorite && currentUserId!=="-1") Res.drawable.red_heart else Res.drawable.black_heart

                        // Check favorite status when the product is displayed
                        LaunchedEffect(product.productId) {
                            if (currentUserId != "-1") {
                                viewModel.checkFavoriteExists(
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
                                            viewModel.toggleFavoriteStatus(
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

@Composable
fun ProductCard(
    shopItems: ShopItems,
    heartRes: Painter,
    onLikeClicked: () -> Unit,
    onProductClicked:(String) -> Unit
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
                        lineHeight = 10.sp,
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

                    Text(
                        text = shopItems.discountMrp.toDiscountFormat(),
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
