package com.pepdeal.infotech.shop

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.BannerCarouselWidget
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.checkPermission
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.jordond.compass.geocoder.MobileGeocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo_new
import pepdealios.composeapp.generated.resources.place_holder


@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(viewModel: ShopViewModal = ViewModals.shopViewModel) {

    // constants
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val snackBar = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    //observer
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSearchLoading by viewModel.isSearchLoading.collectAsStateWithLifecycle()
    val shopListNew by viewModel.shops.collectAsStateWithLifecycle()
    val searchedShopList by viewModel.searchedShops.collectAsStateWithLifecycle()
    val bannerList by viewModel.bannerList.collectAsStateWithLifecycle()

    // variables
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val columnState = rememberLazyListState()
    var locationName: String? by rememberSaveable { mutableStateOf(null) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val latestShopList = rememberUpdatedState(shopListNew)
    val geoLocation = rememberSaveable { Geolocator.mobile() }


    LaunchedEffect(Unit) {
        if (locationName == null) {
            when (val result = geoLocation.current()) {
                is GeolocatorResult.Success -> {
                    println("LOCATION: ${result.data.coordinates}")
                    println(
                        "LOCATION NAME: ${
                            MobileGeocoder()
                                .placeOrNull(result.data.coordinates)?.subLocality
                        }"
                    )
                    locationName =
                        MobileGeocoder().placeOrNull(result.data.coordinates)?.subLocality
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

    LaunchedEffect(latestShopList.value.isEmpty()) {
        if (latestShopList.value.isEmpty()) {
            scope.launch(Dispatchers.IO) {
                viewModel.loadMoreShops()
            }
        }
    }

    LaunchedEffect(bannerList.isEmpty()) {
        if (bannerList.isEmpty()) {
            scope.launch(Dispatchers.IO) {
                viewModel.getTheBannerList()
            }
        }
    }

    LaunchedEffect(searchQuery) {
        snapshotFlow { searchQuery.trim() }
            .debounce(1000)
            .distinctUntilChanged()
            .collectLatest { debouncedQuery ->
                // Call your viewModel function with the debounced search query
                if (debouncedQuery.isNotEmpty()) {
                    viewModel.loadMoreSearchedShops(debouncedQuery)
                }
            }
    }

    LaunchedEffect(columnState) {
        snapshotFlow { columnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = columnState.layoutInfo.totalItemsCount
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 10 && !isLoading && searchQuery.isEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        viewModel.loadMoreShops()
                    }
                }
            }
    }

    LaunchedEffect(Unit) {
        checkPermission(
            permission = Permission.LOCATION,
            controller = controller,
            snackBarHostState = snackBar
        )
        checkPermission(
            permission = Permission.REMOTE_NOTIFICATION,
            controller = controller,
            snackBarHostState = snackBar
        )

    }

    // Outer CardView
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.x != 0f) Offset.Zero else super.onPreScroll(available, source)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    MaterialTheme {
        BindEffect(controller)
        Scaffold(
            snackbarHost = { SnackbarHost(snackBar) }
        ) {
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
                Column(modifier = Modifier.fillMaxSize()) {

                    if (!isSearchActive) {
                        // App Logo
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically // Align items properly
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

                            // Push the next elements to the end of the Row
                            Spacer(modifier = Modifier.weight(1f))

                            // Location Icon with Text
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Location",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = locationName?.toNameFormat() ?: "Fetching...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
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
                                            "Search Shop",
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
                        onExpandedChange = { isSearchActive = it }, // Handles search activation
                    ) {
                        when {
                            isSearchLoading -> {

                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp)
                                        .weight(1f)
                                        .heightIn(max = 300.dp)
                                        .background(Color.White)
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                keyboardController?.hide()
                                            })
                                            detectHorizontalDragGestures { _, _ ->
                                                keyboardController?.hide()
                                            }
                                        }
                                ) {
                                    if (searchedShopList.isEmpty()) {
                                        item {
                                            Text(
                                                text = "No shops found",
                                                modifier = Modifier.padding(16.dp),
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        items(searchedShopList) { shop ->
                                            ShopCardView(shop)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Show Loading Indicator if Needed
                    if (isLoading && shopListNew.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f), // Keeps it centered properly
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Blue)
                        }
                    } else {
                        // Shop List (Properly Weighted)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .nestedScroll(nestedScrollConnection)
                                .padding(top = 5.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        keyboardController?.hide()
                                    })
                                },
                            state = columnState
                        ) {
                            if (bannerList.isNotEmpty()) {
                                item {
                                    BannerCarouselWidget(
                                        bannerList,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            items(shopListNew, key = { it.shop.shopId!! }) { shop ->
                                ShopCardView(shop)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopCardView(shopWithProduct: ShopWithProducts) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    val cardBackgroundColor = Color.fromHex(shopWithProduct.shop.bgColourId ?: "")
    val shopNameColor = Color.fromHex(shopWithProduct.shop.fontColourId)
    val fontResource: FontResource =
        getFontResourceByName(shopWithProduct.shop.fontStyleId ?: "") ?: Res.font.manrope_bold
    val customFont = FontFamily(Font(fontResource))

    Card(
        modifier = Modifier
            .padding(top = 8.dp, start = 5.dp, end = 5.dp, bottom = 0.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Shop Name (Header)
                Box(modifier = Modifier
                    .clickable {
                        NavigationProvider.navController.navigate(
                            Routes.ShopDetails(
                                shopWithProduct.shop.shopId ?: "",
                                Objects.USER_ID
                            )
                        )
                    }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBackgroundColor)
                    ) {
                        Text(
                            text = shopWithProduct.shop.shopName.orEmpty(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 14.sp
                            ),
                            color = shopNameColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
                            textAlign = TextAlign.Center,
                            fontFamily = customFont
                        )

                        // Shop Address
                        Text(
                            text = shopWithProduct.shop.shopAddress2 ?: "",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 12.sp
                            ),
                            color = shopNameColor,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Horizontal RecyclerView (LazyRow)
                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    // Handle drag start
                                    println("Drag started at: $offset")
                                },
                                onDragEnd = {
                                    // Handle drag end
                                    println("Drag ended")
                                },
                                onDragCancel = {
                                    // Handle drag cancel
                                    println("Drag canceled")
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    // Handle horizontal drag
                                    println("Dragged horizontally by: $dragAmount")
                                    offset = Offset(offset.x + dragAmount, offset.y)
                                    change.consume() // Consume the drag to prevent further propagation
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(items = shopWithProduct.products,
                        key = { it.product.productId }) { shopItem ->
                        ShopItemView(shopItem) {
                            NavigationProvider.navController.navigate(Routes.ProductDetailsPage(it))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemView(shopItem: ProductWithImages, onProductClicked: (String) -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .fillMaxHeight()
            .clickable { onProductClicked(shopItem.product.productId) }
            .padding(horizontal = 5.dp, vertical = 5.dp),
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
                            .height(110.dp)
                            .border(1.dp, color = Color.LightGray),
                        imageModel = { shopItem.images.firstOrNull()?.productImages ?: "" },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                setToSaturation(1f)
                            })
                        ),
                        previewPlaceholder = painterResource(Res.drawable.compose_multiplatform),
                        failure = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder),
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop
                            )
                        },
                        loading = {
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
                // Discount Badge (Top-Left)
                if (shopItem.product.discountMrp.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                Color(0xFFFF9800).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(bottomEnd = 8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = shopItem.product.discountMrp.toDiscountFormat(),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = shopItem.product.productName.toNameFormat(),
                fontSize = 13.sp,
                lineHeight = 13.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(
                        start = 3.dp,
                        end = 0.dp,
                        top = 5.dp,
                        bottom = 0.dp
                    )
            )

            if (shopItem.product.onCall == "1") {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(start = 3.dp, end = 0.dp, top = 5.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = shopItem.product.sellingPrice.toTwoDecimalPlaces().toRupee(),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                        color = Color.Black,
                    )

                    Text(
                        text = shopItem.product.mrp.toTwoDecimalPlaces().toRupee(),
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(start = 5.dp).align(Alignment.CenterVertically)
                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                    color = Color.Black,
                    modifier = Modifier.padding(start = 3.dp, end = 0.dp, top = 5.dp)
                )
            }
        }
    }
}
