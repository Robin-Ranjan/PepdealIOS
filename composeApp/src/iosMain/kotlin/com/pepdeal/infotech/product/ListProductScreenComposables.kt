package com.pepdeal.infotech.product

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.CropperStyle
import com.attafitamim.krop.core.crop.DefaultAspectRatios
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import com.pepdeal.infotech.categories.SubCategory
import com.pepdeal.infotech.color.ColorItem
import com.pepdeal.infotech.navigation.routes.Routes
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_light

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProductScreen(viewModal: ProductViewModal = ViewModals.productViewModal) {
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

    // Error message state
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isComposing by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }

    var selectedProductColour = remember { mutableStateOf<List<ColorItem>>(emptyList()) }
    val selectedProductColours =
        viewModal.selectedProductColours.collectAsStateWithLifecycle().value?.toMutableList()

    val productColorsName = selectedProductColours?.joinToString(",") { it.name.toNameFormat() }
    productColors.value = TextFieldValue(productColorsName ?: "")

    var imageBitmap1 = remember { mutableStateOf<ImageBitmap?>(null) }
    var imageBitmap2 = remember { mutableStateOf<ImageBitmap?>(null) }
    var imageBitmap3 = remember { mutableStateOf<ImageBitmap?>(null) }


    var subCategoryToSelect by remember { mutableStateOf<List<SubCategory>>(emptyList()) }

    var showProductPrices by remember { mutableStateOf(false) }

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
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Top App Bar with Back Button
                TopAppBar(
                    title = {
                        Text(
                            text = "Product Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModal.resetTheProductDetails()
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
                    modifier = Modifier.shadow(4.dp),
                    expandedHeight = 50.dp
                )

                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(10.dp),
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
                                            )
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
                                            )
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
//                        onImageSelected = { file ->
//                            imageFile1 = file
//                            imageBitmap1 = loadImage(file)
//                        },
                                    snackBar = snackBar
                                )
                                Spacer(modifier = Modifier.height(16.dp))
//
//                    //second image
                                ImageSelector(
                                    imageState = imageBitmap2,
                                    controller,
                                    picker,
//                        onImageSelected = { file ->
//                            imageFile2 = file
//                            imageBitmap2 = loadImage(file)
//                        },
                                    snackBar = snackBar
                                )
                                Spacer(modifier = Modifier.height(16.dp))
//
//                    // 3rd image
                                ImageSelector(
                                    imageState = imageBitmap3,
                                    controller,
                                    picker,
//                        onImageSelected = { file ->
//                            imageFile3 = file
//                            imageBitmap3 = loadImage(file)
//                        },
                                    snackBar = snackBar
                                )
                            }
                        }

                        item {
                            if (uploading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        uploading = true
//                            CoroutineScope(Dispatchers.IO).launch {
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
                                                        if (imageBitmap1 == null) "" else "Have image"
                                                    )
                                                    put(
                                                        "Product Image 2",
                                                        if (imageBitmap2 == null) "" else "Have image"
                                                    )
                                                    put(
                                                        "Product Image 3",
                                                        if (imageBitmap3 == null) "" else "Have image"
                                                    )
                                                },
                                                setError = { error ->
//                                                    snackBar.showSnackbar(error)
                                                },
                                                status = { status ->

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
//                val file = FileUtil.selectImage()
//                file?.let { onImageSelected(it) }
//                if (file != null) {
//                    onImageSelected(file)
//                } else {
//                    snackBarMessage("Invalid file type! Please select a JPEG, PNG, or WebP image.")
//                }
                scope.launch {
                    requestPermission(
                        controller,
                        permission = Permission.GALLERY,
                        snackBar,
                        picker,
                        imageState = { imageBitMap ->
//                            imageState.value
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
                                    croppedBitmap?.let { imageState.value = it }
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
//            result.let { pickedImage->
//                val originalBitmap = pickedImage.toImageBitmap()
//
//                // Crop the image explicitly with max size 1200x1200
//                val cropResult = imageCropper.crop(maxResultSize = IntSize(1200, 1200), bmp = originalBitmap)
//
//                // Extract the cropped bitmap if successful
//                val croppedBitmap = when (cropResult) {
//                    CropResult.Cancelled -> {
//                        snackBarHostState.showSnackbar("Canceled to crop image", duration = SnackbarDuration.Short)
//                        null
//                    }// No action if user cancels
//                    is CropError -> {
//                        snackBarHostState.showSnackbar("Failed to crop image", duration = SnackbarDuration.Short)
//                        null // Ensure consistency by returning null
//                    }
//                    is CropResult.Success -> cropResult.bitmap // Extract cropped bitmap
//                }
//
//                // Assign the cropped image to state
//                croppedBitmap?.let { imageState.value = it }
//            }
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