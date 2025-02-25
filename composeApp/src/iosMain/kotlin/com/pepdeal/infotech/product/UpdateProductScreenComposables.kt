package com.pepdeal.infotech.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.color.ColorItem
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.TextFieldWithLabel
import com.pepdeal.infotech.util.ColorUtil.colorMap
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.calculateFinalPrice
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    productId: String,
    viewModal: UpdateProductViewModal = ViewModals.updateProductViewModal
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val medialPickerFactory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { medialPickerFactory.createMediaPickerController() }
    val productName = remember { mutableStateOf(TextFieldValue()) }
    val brandName = remember { mutableStateOf(TextFieldValue()) }
    val productCategory = remember { mutableStateOf(TextFieldValue()) }
    val productSubCategory = remember { mutableStateOf(TextFieldValue()) }
    val searchTag = remember { mutableStateOf(TextFieldValue()) }
    val productDescription = remember { mutableStateOf(TextFieldValue()) }
    val productDescription2 = remember { mutableStateOf(TextFieldValue()) }
    val productSpecification = remember { mutableStateOf(TextFieldValue()) }
    val productWarranty = remember { mutableStateOf(TextFieldValue()) }
    val productSize = remember { mutableStateOf(TextFieldValue()) }
    val productColors = remember { mutableStateOf(TextFieldValue()) }
    val productMrp = remember { mutableStateOf(TextFieldValue()) }
    val productDiscount = remember { mutableStateOf(TextFieldValue()) }
    val productSale = remember { mutableStateOf(TextFieldValue()) }
    var showProductPrices by remember { mutableStateOf(false) }
    var productCategoryText by remember { mutableStateOf("") }
    val selectedProductColour = remember { mutableStateOf<List<ColorItem>>(emptyList()) }

    val imageBitmap1 = remember { mutableStateOf<ImageBitmap?>(null) }
    val imageBitmap2 = remember { mutableStateOf<ImageBitmap?>(null) }
    val imageBitmap3 = remember { mutableStateOf<ImageBitmap?>(null) }

    val imageFileList = listOf(imageBitmap1.value, imageBitmap2.value, imageBitmap3.value)

    val snackBar = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isImageUpdated by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val productImages by viewModal.productImages.collectAsStateWithLifecycle()
    val productDetails by viewModal.productDetails.collectAsStateWithLifecycle()
    val registerProductResponse by viewModal.updateProductResponse.collectAsStateWithLifecycle()
    val isUploading by viewModal.isUploading.collectAsStateWithLifecycle()
    LaunchedEffect(productId) {
        viewModal.fetchProductDetails(productId)
        viewModal.fetchProductImages(productId)
    }

    LaunchedEffect(productDetails) {
        productName.value = TextFieldValue(productDetails.productName)
        brandName.value = TextFieldValue(productDetails.brandName)
        productCategory.value = TextFieldValue(productDetails.categoryId)
        productSubCategory.value = TextFieldValue(productDetails.subCategoryId)
        searchTag.value = TextFieldValue(productDetails.searchTag)
        productDescription.value = TextFieldValue(productDetails.description)
        productDescription2.value = TextFieldValue(productDetails.description2)
        productSpecification.value = TextFieldValue(productDetails.specification)
        productWarranty.value = TextFieldValue(productDetails.warranty)
        productSize.value = TextFieldValue(productDetails.sizeName)
        productMrp.value = TextFieldValue(productDetails.mrp)
        productDiscount.value = TextFieldValue(productDetails.discountMrp)
        productSale.value = TextFieldValue(productDetails.sellingPrice)
        showProductPrices = productDetails.onCall == "0"
        productCategoryText = productDetails.categoryId
        // Convert the colorMap into a list of ColorItems for easier filtering
        val colorList = colorMap.map { ColorItem(name = it.key, hexCode = it.value) }

        // Convert to list of hex codes
        val hexCodeList = productDetails.color.split(",").map { it.trim() } ?: emptyList()

        // Map hex codes to their names using the colorList
        val matchedColors = colorList.filter { it.hexCode in hexCodeList }

        // Log the matched colors for debugging
        println("Matched Colors: $matchedColors")

        // Update the UI state with the matched colors
        selectedProductColour.value = matchedColors
        productColors.value = TextFieldValue(matchedColors.joinToString(", ") { it.name })
    }

    LaunchedEffect(Unit) {
        snapshotFlow { registerProductResponse }
            .collectLatest { response ->
                response?.let {
                    if (it.first) {
                        snackBar.showSnackbar("Product Updated Successfully")
                        viewModal.reset()
                        navController.popBackStack()
                    } else {
                        println(it.second)
                        if (it.second.isNotEmpty() && it.second.isNotBlank()) {
                            snackBar.showSnackbar(it.second)
                        }
                    }
                }
            }
    }

    MaterialTheme {
        BindEffect(controller)
        BindMediaPickerEffect(picker)
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
                            "Product Details",
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
                        containerColor = Color.White,  // Background color
                        titleContentColor = Color.Black,  // Title color
                        navigationIconContentColor = Color.Black,  // Back button color
                        actionIconContentColor = Color.Unspecified
                    ),
                    expandedHeight = 50.sdp
                )
            }
        ) { paddingValue ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValue)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            keyboardController?.hide()
                        })
                    }
            ) {

                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(10.sdp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        item {
                            TextFieldWithLabel(label = "Product Name", state = productName)
                        }
                        item {
                            TextFieldWithLabel(label = "Brand Name", state = brandName)
                        }
                        item {
                            TextFieldWithLabel(
                                label = "Product Category",
                                state = productCategory,
                                isEditable = false,
                                onClick = { navController.navigate(Routes.ProductCategoriesBottomSheet) }
                            )
                        }
                        item {
                            TextFieldWithLabel(
                                label = "Product Sub-Category",
                                state = productSubCategory,
                                isEditable = false,
                                onClick = {
                                    if (productCategory.value.text.isNotEmpty()) navController.navigate(
                                        Routes.ProductSubCategoriesBottomSheet
                                    )
                                })
                        }
                        item {
                            Text(
                                text = "Note:- you want search by multiple tags then add search tags separated by (comma)..",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        item {
                            TextFieldWithLabel(
                                label = "Search Tag",
                                state = searchTag,
                                maxLines = 3
                            )
                        }
                        item {
                            TextFieldWithLabel(
                                label = "Product Description",
                                state = productDescription,
                                minLines = 3,
                                maxLines = Int.MAX_VALUE
                            )
                        }
                        item {
                            TextFieldWithLabel(
                                label = "product Description2",
                                state = productDescription2,
                                minLines = 3,
                                maxLines = Int.MAX_VALUE
                            )
                        }
                        item {
                            TextFieldWithLabel(
                                label = "product Specification",
                                state = productSpecification,
                                minLines = 3,
                                maxLines = Int.MAX_VALUE
                            )
                        }

                        item {
                            TextFieldWithLabel(label = "Product Warranty", state = productWarranty)
                        }
                        item {
                            TextFieldWithLabel(label = "Product Sizes", state = productSize)
                        }
                        item {
                            TextFieldWithLabel(
                                label = "Product Colors",
                                state = productColors,
                                maxLines = 5,
                                onClick = { navController.navigate(Routes.MultiColorBottomSheet) },
                                isEditable = false
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(2.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "On Call",
                                    modifier = Modifier.weight(1f)
                                        .padding(start = 5.dp),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Switch(
                                    checked = showProductPrices,
                                    onCheckedChange = { showProductPrices = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Yellow,
                                        checkedTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.padding(end = 5.dp)
                                )
                            }
                        }

                        item {
//                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Conditionally show MRP, Discount, Sale fields
                                if (!showProductPrices) {
                                    TextFieldWithLabel(
                                        label = "MRP",
                                        state = productMrp,
                                        modifier = Modifier.weight(1f).padding(horizontal = 0.dp),
                                        inputType = KeyboardType.Number,
                                        onValueChange = {
                                            calculateFinalPrice(
                                                productMrp.value.text,
                                                productDiscount.value.text,
                                                productSale
                                            ) {
                                                coroutineScope.launch {
                                                    snackBar.showSnackbar("Discount Cant be more than 100%")
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    TextFieldWithLabel(
                                        label = "Discount",
                                        state = productDiscount,
                                        modifier = Modifier.weight(1f).padding(horizontal = 0.dp),
                                        onValueChange = {
                                            calculateFinalPrice(
                                                productMrp.value.text,
                                                productDiscount.value.text,
                                                productSale
                                            ) {
                                                coroutineScope.launch {
                                                    snackBar.showSnackbar("Discount Cant be more than 100%")
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    TextFieldWithLabel(
                                        label = "Sale",
                                        state = productSale,
                                        modifier = Modifier.weight(1f).padding(horizontal = 0.dp),
                                        isEditable = false
                                    )
                                }
                            }
                        }


                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Display images
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .clickable { showDialog = true },
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isImageUpdated) {
                                    productImages.forEach {
                                        CoilImage(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .border(width = 1.dp, color = Color.Gray),
                                            imageModel = { it.productImages },
                                            imageOptions = ImageOptions(
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center
                                            ),
                                            previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
                                        )
                                    }
                                } else {
                                    //first image
                                    ImageSelector(
                                        imageState = imageBitmap1,
                                        controller,
                                        picker,
                                        snackBar = snackBar
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    //second image
                                    ImageSelector(
                                        imageState = imageBitmap2,
                                        controller,
                                        picker,
                                        snackBar = snackBar
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 3rd image
                                    ImageSelector(
                                        imageState = imageBitmap3,
                                        controller,
                                        picker,
                                        snackBar = snackBar
                                    )
                                }
                            }
                        }

                        item {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 5.sdp))
                            } else {
                                Button(
                                    onClick = {
                                        try {
                                            Util.validateShopAndSubmit(
                                                fields = buildMap {
                                                    // Add all fields, including the previously commented ones
                                                    put("Product Name", productName.value.text)
//                                            put("Brand Name", brandName.value.text)
                                                    put(
                                                        "Product Category",
                                                        productSubCategory.value.text
                                                    )
                                                    put(
                                                        "Product Sub-Category",
                                                        productSubCategory.value.text
                                                    )
                                                    put("Search Tag", searchTag.value.text)
                                                    put(
                                                        "Product Description",
                                                        productDescription.value.text
                                                    )
//                                            put("Product Description2", productDescription2.value.text)
//                                            put("Product Specification", productSpecification.value.text)
//                                            put("Product Warranty", productWarranty.value.text)
//                                            put("Product Sizes", productSize.value.text)
//                                            put("Product Colors", productColors.value.text)

                                                    // Conditionally add price-related fields
                                                    if (!showProductPrices) {
                                                        put("Product MRP", productMrp.value.text)
                                                        put(
                                                            "Product Discount",
                                                            productDiscount.value.text
                                                        )
                                                        put("Product Sale", productSale.value.text)
                                                    }

                                                    // Add image-related fields
                                                    if (isImageUpdated) {
                                                        put(
                                                            "Product Image 1",
                                                            if (imageBitmap1.value == null) "" else "Have image"
                                                        )
                                                        put(
                                                            "Product Image 2",
                                                            if (imageBitmap2.value == null) "" else "Have image"
                                                        )
                                                        put(
                                                            "Product Image 3",
                                                            if (imageBitmap3.value == null) "" else "Have image"
                                                        )
                                                    }
                                                },
                                                setError = { error ->
                                                    println("error:- $error")
                                                    if (error.isNotEmpty() && error.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            snackBar.showSnackbar(error)
                                                        }
                                                    }
                                                },
                                                status = { status ->
                                                    if (status) {
                                                        viewModal.updateProductDetails(
                                                            productId = productId,
                                                            updatedProductMaster = ProductMaster(
                                                                productId = "",
                                                                userId = Objects.USER_ID,
                                                                shopId = Objects.SHOP_ID,
                                                                productName = productName.value.text,
                                                                brandId = "",
                                                                brandName = brandName.value.text,
                                                                categoryId = productCategory.value.text,
                                                                subCategoryId = productSubCategory.value.text,
                                                                description = productDescription.value.text,
                                                                description2 = productDescription2.value.text,
                                                                specification = productSpecification.value.text,
                                                                warranty = productWarranty.value.text,
                                                                sizeId = productSize.value.text,
                                                                sizeName = "",
                                                                color = productColors.value.text,
                                                                searchTag = searchTag.value.text,
                                                                onCall = if (showProductPrices) "0" else "1",
                                                                mrp = productMrp.value.text,
                                                                discountMrp = productDiscount.value.text,
                                                                sellingPrice = productSale.value.text,
                                                                isActive = "1",
                                                                flag = "1",
                                                                createdAt = Util.getCurrentTimeStamp(),
                                                                updatedAt = Util.getCurrentTimeStamp()
                                                            ),
                                                            isImageEdited = isImageUpdated,
                                                            newUriList = imageFileList.filterNotNull().toMutableList()
                                                        )
                                                    }
                                                }
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Text(text = "Upload", fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                title = {
                    Text(
                        text = "Update Image",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                },
                text = {
                    Text(
                        text = "Updating the image will remove all existing images. Do you wish to continue?",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isImageUpdated = true
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Yes, Update",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 14.sp
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDialog = false },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 14.sp
                        )
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TextFieldWithChips(
    state: MutableState<TextFieldValue>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.value,
            onValueChange = { newText -> state.value = newText },
            label = { Text(text = label) }
        )
        // Place additional UI elements below the text field.
        if (state.value.text.isNotEmpty()) {
            ColorChips(
                colorCodes = state.value.text,
                colorMap
            )
        }
    }
}


//@Composable
//fun TextFieldWithLabelAndColorChips(
//    label: String,
//    maxLines: Int = 1,
//    fontResources: FontResource = Res.font.manrope_medium,
//    state: MutableState<TextFieldValue>,
//    isEditable: Boolean = true,
//    maxLength: Int = Int.MAX_VALUE,
//    inputType: KeyboardType = KeyboardType.Text,
//    minLines: Int = 1,
//    modifier: Modifier = Modifier.fillMaxWidth(),
//    onValueChange: (TextFieldValue) -> Unit = {},
//    color: Color = Color.Black,
//    onClick: () -> Unit = {}
//) {
//    // A predefined map of color codes to names.
//    val colorNameMap = mapOf(
//        "FF0000" to "Red",
//        "00FF00" to "Green",
//        "0000FF" to "Blue"
//        // Add additional mappings as needed.
//    )
//
//    OutlinedTextField(
//        value = state.value,
//        label = { Text(text = label, color = Color.Gray) },
//        onValueChange = { newText ->
//            if (inputType == KeyboardType.Number) {
//                if (newText.text.all { it.isDigit() } && newText.text.length <= maxLength) {
//                    state.value = newText
//                }
//            } else {
//                if (newText.text.length <= maxLength) {
//                    state.value = newText
//                }
//            }
//            onValueChange(newText)
//        },
//        modifier = modifier
//            .background(Color.White, MaterialTheme.shapes.small)
//            .clickable { onClick() }
//            .padding(8.dp),
//        textStyle = TextStyle(
//            color = color,
//            fontSize = 15.sp,
//            fontFamily = FontFamily(Font(fontResources)),
//            lineHeight = 15.sp
//        ),
//        enabled = isEditable,
//        maxLines = maxLines,
//        minLines = minLines,
//        keyboardOptions = KeyboardOptions.Default.copy(
//            keyboardType = inputType
//        ),
//        decorationBox = { innerTextField ->
//            Column {
//                innerTextField()
//                if (state.value.text.isNotEmpty()) {
//                    ColorChips(
//                        colorCodes = state.value.text,
//                        colorNameMap = colorNameMap
//                    )
//                }
//            }
//        }
//    )
//}

// Helper composable to display a row of color chips.
@Composable
fun ColorChips(
    colorCodes: String,
    colorNameMap: Map<String, String>
) {
    // Split the string into individual codes, trimming whitespace.
    val colorList = colorCodes.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colorList.forEach { code ->
            // Look up the name; use "Unknown" if not found.
            val colorName = colorNameMap[code] ?: "Unknown"
            val color = parseColor(code)
            ColorChip(color = color, name = colorName)
        }
    }
}

// A simple chip composable showing a colored circle and a label.
@Composable
fun ColorChip(color: Color, name: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // A small circle filled with the specified color.
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color = color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = name, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Helper function to parse a hex string into a Color.
fun parseColor(hex: String): Color {
    return try {
        // If the hex code is 6 characters, add full alpha.
        val colorInt = if (hex.length == 6) {
            (0xFF shl 24) or hex.toLong(16).toInt()
        } else {
            hex.toLong(16).toInt()
        }
        Color(colorInt)
    } catch (e: Exception) {
        Color.Gray // Fallback color if parsing fails.
    }
}