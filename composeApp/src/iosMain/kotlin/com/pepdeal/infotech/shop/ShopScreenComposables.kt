package com.pepdeal.infotech.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.pepdeal.infotech.LocationViewModel
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.locationPermissionController
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.SearchView
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.black_heart
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.pepdeal_logo
import pepdealios.composeapp.generated.resources.place_holder
import dev.icerock.moko.geo.LocationTracker
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp


@OptIn(FlowPreview::class)
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
    val shopListNew by viewModel.shops.collectAsStateWithLifecycle()
    val searchedShopList by viewModel.searchedShops.collectAsStateWithLifecycle()
    val bannerList by viewModel.bannerList.collectAsStateWithLifecycle()

    // variables
    var searchQuery by remember { mutableStateOf("") }
    val columnState = rememberLazyListState()
    var locationName by remember { mutableStateOf("") }


    val latestShopList = rememberUpdatedState(shopListNew)
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
        snapshotFlow { searchQuery }
            .debounce(1000)                     // Wait 300ms after the last change
            .distinctUntilChanged()              // Only react if the value has actually changed
            .collectLatest { debouncedQuery ->
                // Call your viewModel function with the debounced search query
                viewModel.loadMoreSearchedShops(debouncedQuery)
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
        scope.launch {
            locationPermissionController(
                controller,
                snackBar = snackBar,
                scope
            )
        }
    }

    val displayedProductList by derivedStateOf {
        if (searchQuery.isNotEmpty()) {
            searchedShopList
        } else {
            shopListNew
        }
    }

    // Outer CardView
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.x != 0f) Offset.Zero else super.onPreScroll(available, source)
            }
        }
    }

    MaterialTheme {
        BindEffect(controller)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(Res.drawable.pepdeal_logo),
                        contentDescription = "Your image description",
                        modifier = Modifier
                            .width(130.dp)
                            .height(28.dp)
                            .padding(start = 5.dp),
                        contentScale = ContentScale.FillBounds // Adjust based on your needs (e.g., FillBounds, Fit)
                    )
                }

                SearchView("Search Shop", searchQuery) {
                    searchQuery = it
                }
                Text(text = if (searchQuery.isEmpty()) shopListNew.size.toString() else searchedShopList.size.toString())

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .padding(0.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->

                            }
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
                    items(displayedProductList,
                        key = { it.shop.shopId!! }
                    ) { shop ->
                        // Shop Card
                        ShopCardView(shop)
                    }
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    color = Color.Blue
                )
            }
        }
    }
}

@Composable
fun ShopCardView(shopWithProduct: ShopWithProducts) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    // Card background color
    val cardBackgroundColor = Color.fromHex(shopWithProduct.shop.bgColourId ?: "")
    val shopNameColor = Color.fromHex(shopWithProduct.shop.fontColourId)

    val fontResource: FontResource =
        getFontResourceByName(shopWithProduct.shop.fontStyleId ?: "") ?: Res.font.manrope_bold

    val customFont = FontFamily(Font(fontResource))


    Card(
        modifier = Modifier
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 0.dp)
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
                            .background(cardBackgroundColor) // Apply consistent background to the Box
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
                                .fillMaxWidth() // Makes the Text fill the available width
                                .padding(top = 5.dp),
                            textAlign = TextAlign.Center,// Centers the text within the available width
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
                                .fillMaxWidth(), // Makes the Text fill the available width
                            textAlign = TextAlign.Center // Centers the text within the available width
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
                    modifier = Modifier.fillMaxWidth() // Ensure ImageView fills the width
                ) {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxWidth() // Ensure the ImageView stretches to the full width of the Card
                            .height(110.dp)
                            .border(
                                1.dp,
                                color = Color.LightGray
                            ), // Set a fixed height for the image
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
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        },
                        loading = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                }
            }

            // Product Name as ImageView, stretching the width of the Card
            Text(
                text = shopItem.product.productName.toNameFormat(),
                fontSize = 11.sp,
                lineHeight = 11.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(
                        start = 3.dp,
                        end = 0.dp,
                        top = 5.dp,
                        bottom = 0.dp
                    ) // Padding for sides of the text
            )

            if (shopItem.product.onCall == "1") {
                Row(
//                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.padding(start = 3.dp, end = 0.dp, top = 5.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = shopItem.product.sellingPrice.toTwoDecimalPlaces().toRupee(),
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(0.dp)
                    )

                    Text(
                        text = shopItem.product.mrp.toTwoDecimalPlaces().toRupee(),
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    Text(
                        text = shopItem.product.discountMrp.toDiscountFormat(),
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 3.dp, end = 0.dp, top = 5.dp)
                )
            }
        }
    }
}
