package com.pepdeal.infotech.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import com.pepdeal.infotech.checkPermission
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.core.basic_ui.AppSearchBar
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.shop.screen.component.SearchTagItemCard
import com.pepdeal.infotech.shop.viewModel.ShopViewModel
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
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
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.pepdeal_logo
import pepdealios.composeapp.generated.resources.place_holder
import platform.Foundation.NSUUID

@Composable
fun ShopScreenRoot(viewModel: ShopViewModel = koinViewModel()) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val searchTags by viewModel.searchTags.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
            }
            viewModel.onAction(ShopViewModel.Action.OnResetMessage)
        }
    }

    ShopScreen(
        uiState,
        searchTags,
        viewModel = viewModel,
        snackbarHostState = snackBarHostState,
        onAction = viewModel::onAction,
        newAddress = uiState.address?.address
    )
}

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    uiState: ShopViewModel.UiState,
    searchTags: ShopViewModel.SearchShopTags,
    newAddress: String? = null,
    viewModel: ShopViewModel,
    snackbarHostState: SnackbarHostState,
    onAction: (ShopViewModel.Action) -> Unit,
) {

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // variables
    val columnState = rememberLazyListState()
    var locationName: String? by rememberSaveable { mutableStateOf(null) }
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

                    if (uiState.shops.isEmpty()) {
                        viewModel.loadMoreShops(
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

    LaunchedEffect(Unit) {
        checkPermission(
            permission = Permission.LOCATION,
            controller = controller,
            snackBarHostState = snackbarHostState
        )
        checkPermission(
            permission = Permission.REMOTE_NOTIFICATION,
            controller = controller,
            snackBarHostState = snackbarHostState
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
                Column(modifier = Modifier.fillMaxSize()) {

                    if (!isSearchActive) {
                        // App Logo
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                                viewModel.onAction(ShopViewModel.Action.OnLocationClick)
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
                        onSearchQueryChange = { onAction(ShopViewModel.Action.OnSearchQueryChange(it)) },
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it },
                        isSearchLoading = searchTags.isLoading,
                        onSearchTriggered = {
                            NavigationProvider.navController.navigate(
                                Routes.ShopSearchRoute(
                                    it,
                                    uiState.user?.userId
                                )
                            )
                        },
                        suggestionsList = suggestions,
                        onClearClick = { onAction(ShopViewModel.Action.OnSearchQueryChange("")) }
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
                                                    Routes.ShopSearchRoute(
                                                        queryTag,
                                                        uiState.user?.userId
                                                    )
                                                )
                                            }
                                        }

                                        if (searchTags.topShopNames.isNotEmpty()) {
                                            item {
                                                Text(
                                                    text = "Search By Shop Name",
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

                                        items(searchTags.topShopNames) {
                                            SearchTagItemCard(it) {
                                                NavigationProvider.navController.navigate(
                                                    Routes.ShopSearchRoute(
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
                        Column(
                            modifier = Modifier.fillMaxSize().background(BackGroundColor)
                                .padding(top = 5.dp)
                        ) {
                            // 🔹 Always show banners if available
                            if (uiState.bannerList.isNotEmpty()) {
                                BannerCarouselWidget(
                                    uiState.bannerList,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            when {
                                uiState.isLoading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.Blue)
                                    }
                                }

                                uiState.isEmpty -> {
                                    // 🔸 Show empty state message
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("PepDeal isn’t available on this location just yet.")
                                    }
                                }

                                else -> {
                                    // 🔹 Show shop list
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .nestedScroll(nestedScrollConnection)
                                            .padding(top = 5.dp),
                                        state = columnState
                                    ) {
                                        items(
                                            uiState.shops,
                                            key = { it.shop.shopId ?: NSUUID.UUID().toString() }
                                        ) { shop ->
                                            ShopCardView(shop, onShopClicked = { shopId ->
                                                NavigationProvider.navController.navigate(
                                                    Routes.ShopDetails(
                                                        shopId = shopId,
                                                        userId = uiState.user?.userId ?: "-1"
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
}

@Composable
fun ShopCardView(shopWithProduct: ShopWithProducts, onShopClicked: (String) -> Unit) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
//    val cardBackgroundColor = Color.fromHex(shopWithProduct.shop.bgColourId ?: "")
    val cardBackgroundColor = Color.White
//    val shopNameColor = Color.fromHex(shopWithProduct.shop.fontColourId)
    val shopNameColor = Color.Black
    val fontResource: FontResource =
//        getFontResourceByName(shopWithProduct.shop.fontStyleId ?: "") ?: Res.font.manrope_bold
        Res.font.manrope_bold
    val customFont = FontFamily(Font(fontResource))

    Card(
        modifier = Modifier
            .padding(top = 8.dp, start = 5.dp, end = 5.dp, bottom = 0.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.25.dp, Color.Gray.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Shop Name (Header)
                Box(
                    modifier = Modifier
                        .clickable {
                            onShopClicked(shopWithProduct.shop.shopId ?: "")
                        }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBackgroundColor)
                    ) {
                        Text(
                            text = shopWithProduct.shop.shopName.orEmpty().uppercase(),
                            color = shopNameColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp, start = 8.dp),
                            textAlign = TextAlign.Start,
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
                                .fillMaxWidth().padding(start = 8.dp),
                            textAlign = TextAlign.Start
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
                    items(
                        items = shopWithProduct.products,
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
//        border = BorderStroke(0.dp, Color.Gray)
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
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(0.dp, color = Color.Gray)
                ) {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
//                            .border(1.dp, color = Color.LightGray),
                        imageModel = { shopItem.images.firstOrNull()?.productImages ?: "" },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.FillBounds,
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
                                contentScale = ContentScale.FillBounds
                            )
                        },
                        loading = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder),
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.FillBounds
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
                                Color(0xFFFF9800).copy(alpha = 0.7f),
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

val BackGroundColor = Color(0xFFF7F7F7)
