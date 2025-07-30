package com.pepdeal.infotech.product.producrDetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.TicketDialog
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.producrDetails.component.ProductImagesCarouselWidget
import com.pepdeal.infotech.product.producrDetails.component.TicketFloatingActionButton
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_regular
import pepdealios.composeapp.generated.resources.original

@Composable
fun ProductDetailScreenRoot(viewModal: ProductDetailsViewModal = koinViewModel()) {
    val uiState by viewModal.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it.message.isNotBlank()) {
                snackbarHostState.showSnackbar(it.message)
                viewModal.onAction(ProductDetailsViewModal.Action.OnResetMessage)
            }
        }
    }

    ProductDetailScreen(
        uiState = uiState,
        onAction = viewModal::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailsViewModal.UiState,
    onAction: (ProductDetailsViewModal.Action) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    MaterialTheme {
        Scaffold(
            floatingActionButton = {
                if (uiState.product != null) {
                    TicketFloatingActionButton(
                        isTicketExists = uiState.product.isTicketActive,
                        onClick = {
                            onAction(ProductDetailsViewModal.Action.OnClickOfFloatingButton)
                        }
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.EndOverlay,
            containerColor = Color.White,
            snackbarHost = {
                CustomSnackBarHost(
                    hostState = snackbarHostState,
                    currentMessage = uiState.message
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(modifier = Modifier.fillMaxSize()) {

                    when {
                        uiState.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }

                        uiState.isError -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.align(Alignment.Center)) {
                                    Text(text = "Something went wrong")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {},
                                    ) {
                                        Text(text = "Retry")
                                    }

                                    Button(
                                        onClick = { NavigationProvider.navController.popBackStack() },
                                    ) {
                                        Text(text = "Back")
                                    }
                                }
                            }
                        }

                        else -> {
                            uiState.product?.let { product ->
                                LazyColumn {
                                    item {
                                        ProductImagesCarouselWidget(
                                            productImages = product.product.images,
                                            isfav = product.isFavourite,
                                            onBackClicked = {
                                                NavigationProvider.navController.popBackStack()
                                            },
                                            onLikeClick = {
                                                onAction(ProductDetailsViewModal.Action.OnFavClick)
                                            }
                                        )
                                    }

                                    item {
                                        ProductInfoSection(product.product.product)
                                    }
                                    item {
                                        WarrantySection(product.product.product)
                                    }

                                    item {
                                        AdditionalDetailsSection(product.product.product)
                                    }

                                    item {
                                        ProductShopInfoSection(product.shop, uiState.userId ?: "-1")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showDialog && uiState.product != null && uiState.userId != null) {
            TicketDialog(
                productDetails = uiState.product.product.product,
                showDialog = uiState.showDialog,
                onDismiss = { onAction(ProductDetailsViewModal.Action.OnChangeDialogState(false)) },
                onSubmitTicket = { newTicket ->
                    newTicket.copy(userId = uiState.userId)
                    onAction(ProductDetailsViewModal.Action.AddTicket(newTicket))
                }
            )
        }
    }
}

@Composable
fun ProductInfoSection(product: ProductMaster) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = product.productName.toNameFormat(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            if (product.onCall == "1") {
                Text(
                    text = product.discountMrp.toDiscountFormat(),
                    fontSize = 16.sp,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.sellingPrice.toTwoDecimalPlaces().toRupee(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.mrp.toTwoDecimalPlaces().toRupee(),
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.LineThrough,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "On Call".toNameFormat(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun WarrantySection(product: ProductMaster) {

    val colorNames = if (product.color.isNotBlank() && product.color != "-") {
        product.color.split(",")
            .mapNotNull { code -> ColorUtil.colorMap.entries.find { it.value == code }?.key }
            .joinToString(",") // Corrected joinToString usage
            .toNameFormat()
    } else {
        null
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Warranty:${product.warranty}",
            fontSize = 13.sp,
            color = Color.DarkGray,
            lineHeight = 15.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.original),
                contentDescription = "Original",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                colorNames?.let {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Color: ")
                            }
                            withStyle(style = SpanStyle(color = Color.DarkGray)) {
                                append(it.toNameFormat())
                            }
                        },
                        fontSize = 14.sp
                    )
                }
                if (product.brandId.isNotEmpty() && product.brandId.isNotEmpty() && product.brandId != "-") {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Brand: ")
                            }
                            withStyle(style = SpanStyle(color = Color.DarkGray)) {
                                append(product.brandId.toNameFormat())
                            }
                        },
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Category: ")
                        }
                        withStyle(style = SpanStyle(color = Color.DarkGray)) {
                            append(product.categoryId.toNameFormat())
                        }
                    },
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AdditionalDetailsSection(product: ProductMaster) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Product Description",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(
                Font(Res.font.manrope_bold)
            )
        )
        Text(
            text = product.description.toNameFormat(),
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontFamily = FontFamily(Font(Res.font.manrope_regular))
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Product Specifications", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = product.specification.toNameFormat(), fontSize = 12.sp, color = Color.DarkGray)

        if (product.description2.isNotEmpty() || product.description2.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Additional Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = product.description2.toNameFormat(),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ProductShopInfoSection(shopMaster: ShopMaster, userID: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Shop Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(
                Font(Res.font.manrope_bold)
            ),
            lineHeight = 18.sp
        )
        Text(
            modifier = Modifier.clickable(
                onClick = {
                    shopMaster.shopId?.let {
                        NavigationProvider.navController.navigate(
                            Routes.ShopDetails(it, userId = userID)
                        )
                    }
                }
            ),
            text = shopMaster.shopName?.toNameFormat() ?: "",
            fontSize = 12.sp,
            color = Color.DarkGray,
            lineHeight = 14.sp,
            fontFamily = FontFamily(Font(Res.font.manrope_bold))
        )

        // Row for Mobile Number with Call Icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = Color.DarkGray,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
            Text(
                text = shopMaster.shopMobileNo,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = Color.DarkGray,
                fontFamily = FontFamily(Font(Res.font.manrope_regular))
            )
        }

        // Row for Address with Location Icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Location",
                tint = Color.DarkGray,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
            Text(
                text = shopMaster.shopAddress ?: "",
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = Color.DarkGray,
                fontFamily = FontFamily(Font(Res.font.manrope_regular))
            )
        }
    }
}

@Composable
fun ProductImageWidget(
    imageUrl: String,
) {
    CoilImage(
        modifier = Modifier
            .fillMaxSize(),
        imageModel = { imageUrl },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        ),
        previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
    )
}


