package com.pepdeal.infotech.product.addProduct

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.DefaultAspectRatios
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.pepdeal.infotech.ImageCompressor
import com.pepdeal.infotech.categories.SubCategory
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.shop.TextFieldWithLabel
import com.pepdeal.infotech.util.CategoriesUtil
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.calculateFinalPrice
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.media.compose.toImageBitmap
import dev.icerock.moko.media.picker.CanceledException
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewProductScreen(shopId:String,viewModal: AddNewProductViewModal = ViewModals.addNewProductViewModal) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val coroutineScope = rememberCoroutineScope()

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
    val selectedProductColours =
        viewModal.selectedProductColours.collectAsStateWithLifecycle().value?.toMutableList()

    val productColorsName = selectedProductColours?.joinToString(",") { it.name.toNameFormat() }
    val productColorsCode = selectedProductColours?.joinToString(",") { it.hexCode }
    productColors.value = TextFieldValue(productColorsName ?: "")

    val imageBitmap1 = remember { mutableStateOf<ImageBitmap?>(null) }
    val imageBitmap2 = remember { mutableStateOf<ImageBitmap?>(null) }
    val imageBitmap3 = remember { mutableStateOf<ImageBitmap?>(null) }

    val imageFileList = listOf(imageBitmap1.value, imageBitmap2.value, imageBitmap3.value)
    var subCategoryToSelect by remember { mutableStateOf<List<SubCategory>>(emptyList()) }

    var showProductPrices by remember { mutableStateOf(false) }

    // Observe response state from ViewModel
    val registerProductResponse by viewModal.registerProductResponse.collectAsStateWithLifecycle()
    val isUploading by viewModal.isUploading.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    productCategory.value = TextFieldValue(
        viewModal.selectedProductCategories.collectAsStateWithLifecycle().value?.name ?: ""
    )

    productSubCategory.value = TextFieldValue(
        viewModal.selectedProductSubCategories.collectAsStateWithLifecycle().value?.name ?: ""
    )
    val snackBar = remember { SnackbarHostState() }
    LaunchedEffect(viewModal.selectedProductCategories.value?.id) {
        viewModal.resetTheSelectedSubCategories()
        subCategoryToSelect =
            CategoriesUtil.getSubCategoriesListById(
                viewModal.selectedProductCategories.value?.id ?: -1
            )
    }

    LaunchedEffect(showProductPrices) {
        if (!showProductPrices) {
            // Reset values when product prices are not visible
            productMrp.value = TextFieldValue("")
            productDiscount.value = TextFieldValue("")
            productSale.value = TextFieldValue("")
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { registerProductResponse }
            .collectLatest { response ->
                response?.let {
                    if (it.first) {
                        Util.showToast("Product Registered Successfully")
                        viewModal.reset()
                        navController.popBackStack()
                    } else {
                        println(it.second)
                        if(it.second.isNotEmpty() && it.second.isNotBlank()){
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
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
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
                        awaitEachGesture {
                            awaitFirstDown()
                            keyboardController?.hide()
                        }
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
                                verticalAlignment = Alignment.CenterVertically // Aligns content vertically
                            ) {
                                Text(
                                    text = "On Call",
                                    modifier = Modifier.weight(1f)
                                        .padding(start = 5.dp), // Makes the text take available space
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
                            Spacer(modifier = Modifier.height(8.dp))
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
                            Spacer(modifier = Modifier.height(16.dp))
                            // Display images
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
//                    //first image
                                ImageSelector(
                                    imageState = imageBitmap1,
                                    controller,
                                    picker,
                                    snackBar = snackBar
                                )
                                Spacer(modifier = Modifier.height(16.dp))
//
//                    //second image
                                ImageSelector(
                                    imageState = imageBitmap2,
                                    controller,
                                    picker,
                                    snackBar = snackBar
                                )
                                Spacer(modifier = Modifier.height(16.dp))
//
//                    // 3rd image
                                ImageSelector(
                                    imageState = imageBitmap3,
                                    controller,
                                    picker,
                                    snackBar = snackBar
                                )
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
                                                },
                                                setError = { error ->
                                                    println("error:- $error")
                                                    if(error.isNotEmpty() && error.isNotBlank()){
                                                        coroutineScope.launch {
                                                            snackBar.showSnackbar(error)
                                                        }
                                                    }
                                                },
                                                status = { status ->
                                                    if (status) {
                                                            viewModal.registerProduct(
                                                                shopId = shopId,
                                                                productMaster = ProductMaster(
                                                                    productId = "",
                                                                    userId = "",
                                                                    shopId = shopId,
                                                                    productName = productName.value.text,
                                                                    brandId = "",
                                                                    brandName = brandName.value.text,
                                                                    categoryId = productCategory.value.text.toNameFormat().trim(),
                                                                    subCategoryId = productSubCategory.value.text.toNameFormat().trim(),
                                                                    description = productDescription.value.text,
                                                                    description2 = productDescription2.value.text,
                                                                    specification = productSpecification.value.text,
                                                                    warranty = productWarranty.value.text,
                                                                    sizeId = productSize.value.text,
                                                                    sizeName = "",
                                                                    color = productColorsCode?:"",
                                                                    searchTag = searchTag.value.text,
                                                                    onCall = if(showProductPrices) "0" else "1",
                                                                    mrp = productMrp.value.text,
                                                                    discountMrp = productDiscount.value.text,
                                                                    sellingPrice = productSale.value.text,
                                                                    isActive = "1",
                                                                    flag = "1",
                                                                    createdAt = Util.getCurrentTimeStamp(),
                                                                    updatedAt = Util.getCurrentTimeStamp()
                                                                ),
                                                                uriList = imageFileList.filterNotNull().toMutableList()
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
    }
}

@Composable
fun ImageSelector(
    imageState: MutableState<ImageBitmap?>,
    controller: PermissionsController,
    picker: MediaPickerController,
    snackBar: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White)
            .border(1.dp, color = Color.Gray)
            .clickable {
                scope.launch {
                    requestPermission(
                        controller,
                        permission = Permission.GALLERY,
                        snackBar,
                        picker,
                        imageState = { imageBitMap ->
                            scope.launch {
                                imageBitMap.let {
                                    val result = imageCropper.crop(
                                        maxResultSize = IntSize(1200, 1200),
                                        bmp = imageBitMap
                                    ) // Suspends until user accepts or cancels cropping
                                    val croppedBitmap = when (result) {
                                        CropResult.Cancelled -> null
                                        is CropError -> null
                                        is CropResult.Success -> {
                                            result.bitmap
                                        }
                                    }
                                    croppedBitmap?.let { bitMap ->
                                        val compressedBitmap = ImageCompressor().compress(bitMap,60 * 1024)
                                        imageState.value = compressedBitmap
                                    }
                                }
                            }
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val cropState = imageCropper.cropState
        if (cropState != null) ImageCropperDialog(
            state = cropState,
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            style = cropperStyle(
                backgroundColor = Color.Yellow,
                autoZoom = false,
                aspects = DefaultAspectRatios
            )
        )
        if (imageState.value != null) {
            Image(bitmap = imageState.value!!, contentDescription = "Selected Image")
        } else {
            Text(
                text = "Click to Select",
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily(
                    Font(
                        Res.font.manrope_light
                    )
                )
            )
        }
    }
}

suspend fun requestPermission(
    controller: PermissionsController,
    permission: Permission,
    snackBarHostState: SnackbarHostState,
    picker: MediaPickerController,
    imageState: (ImageBitmap) -> Unit,
) {

    val isGranted = controller.isPermissionGranted(permission)
    if (isGranted) {
        try {
            val result = picker.pickImage(MediaSource.GALLERY)
            imageState.invoke(result.toImageBitmap())
        } catch (e: CanceledException) {
            snackBarHostState.showSnackbar("No image Selected", duration = SnackbarDuration.Short)
        }
    } else {
        try {
            val permissionState = controller.getPermissionState(permission)
            if (permissionState == PermissionState.Denied) {
                controller.providePermission(permission)
            } else if (permissionState == PermissionState.DeniedAlways) {
                val result = snackBarHostState.showSnackbar(
                    message = "Permission denied. Open settings to grant it.",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    controller.openAppSettings()
                }
            } else {
                controller.providePermission(permission)
            }
        } catch (e: DeniedException) {
            snackBarHostState.showSnackbar("Permission denied", duration = SnackbarDuration.Short)
        } catch (e: DeniedAlwaysException) {
            val result = snackBarHostState.showSnackbar(
                message = "Permission denied. Open settings to grant it.",
                actionLabel = "Settings",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                controller.openAppSettings()
            }
        } catch (e: RequestCanceledException) {
            snackBarHostState.showSnackbar("Permission request Canceled ")
        }
    }
}