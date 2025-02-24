package com.pepdeal.infotech.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.rememberDataStore
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAllProductScreen(
    shopId: String,
    viewModal: ListAllProductViewModal = ViewModals.listAllProductViewModal
) {
    val datastore = DataStore.dataStore
    val myKey = PreferencesKeys.MOBILE_NO
    // Collect the data from DataStore as state.
    val preferencesFlow = datastore.data
    // Provide an initial empty preferences in case no value is stored yet.
    val preferences by preferencesFlow.collectAsState(initial = emptyPreferences())

    // Retrieve the current value (or default to "Default Value").
    var currentValue: String

    val productsWithImages by viewModal.productWithImages.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()
    val snackBar = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shopId) {
        if (productsWithImages.isEmpty()) {
            viewModal.getAllProduct(shopId)
        }
    }
    LaunchedEffect(Unit) {
        var pref = "Default"
        datastore.data.map {
            pref = it[myKey] ?: "Default"
        }.flowOn(Dispatchers.IO)
        println(pref)
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBar, snackbar = { data ->
                    Snackbar(action = {
                        TextButton(onClick = { data.visuals.message }) {
                            Text("Okay", color = Color.Yellow)  // Action button color
                        }
                    },
                        containerColor = Color.Yellow,
                        contentColor = Color.Black,
                        content = { Text(text = data.visuals.message) })
                })
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
                            viewModal.reset()
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
                if (isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(items = productsWithImages,
                            key = { it.product.productId }) { products ->
                            var isLive by remember { mutableStateOf(products.product.isActive == "0") }
                            ProductCard(products,
                                isLive = isLive,
                                onSwitchChanged = { newState ->
                                    isLive = newState
                                    viewModal.updateProductStatusByShopOwner(
                                        products.product.productId,
                                        if (newState) "0" else "1"
                                    )
                                },
                                onUpdateClick = {
                                    coroutineScope.launch {
                                        datastore.edit {
                                            it.clear()
                                        }
                                    }
                                    navController.navigate(Routes.UpdateProductPage(products.product.productId))
                                }, onRemoveClick = {
//                                    coroutineScope.launch {
//                                        datastore.edit { prefs ->
//                                            prefs[myKey] = "User Id Updated"
//                                        }
//                                    }

                                    currentValue = preferences[myKey] ?: "Default Value"
                                    println(currentValue)
//                                    coroutineScope.launch {
//                                        datastore.edit {
//                                            it.clear()
//                                        }
//                                    }
//                                    currentValue = preferences[myKey] ?: "Default Value"
//                                    println(currentValue)
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
    productWithImages: ProductWithImages,
    isLive: Boolean,
    onSwitchChanged: (Boolean) -> Unit,
    onUpdateClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val product = productWithImages.product
    val productImages = productWithImages.images
//    val isLive by remember { mutableStateOf(product.isActive == "0") }

    val sellingPrice =
        if (product.onCall == "1") product.sellingPrice.toTwoDecimalPlaces() else "On Call"

    // Outer Card mimics MaterialCardView with margin and corner radius.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium, // You can customize the corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Use a vertical layout with padding.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            // Top section: product image and details.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                CoilImage(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(width = 1.dp, color = Color.Gray),
                    imageModel = { productImages.firstOrNull()?.productImages },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
                )
                Spacer(modifier = Modifier.width(5.dp))
                // Product Details Column
                Column(modifier = Modifier.weight(1f)) {
                    // First Row: Product Name and Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.productName.toNameFormat(),
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 16.sp,
                            color = Color.Black
                            // fontFamily = yourCustomFontFamily if needed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isLive,
                            onCheckedChange = onSwitchChanged,
                            modifier = Modifier.padding(top = 0.dp, start = 10.dp, end = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Second Row: Sell Price and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sellingPrice,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "status",
                            modifier = Modifier.padding(horizontal = 5.dp),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            // Divider line (2dp height)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Bottom section: Update and Remove actions.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Update action
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { onUpdateClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Update",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(Color.DarkGray)
                )
                // Remove action
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onRemoveClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Remove",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}