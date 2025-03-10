package com.pepdeal.infotech.shop

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.Objects
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.placeAPI.PlacesSearchScreen
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.States
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.flow.collectLatest
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenYourShopScreen(shopPhoneNo :String,viewModel: OpenYourShopViewModal = ViewModals.openYOurShopViewModal) {
    val shopName = remember { mutableStateOf(TextFieldValue()) }
    val shopAddress = remember { mutableStateOf(TextFieldValue()) }
    val signBoardAddress = remember { mutableStateOf(TextFieldValue()) }
    val shopState = remember { mutableStateOf(TextFieldValue()) }
    val shopCity = remember { mutableStateOf(TextFieldValue()) }
    val shopArea = remember { mutableStateOf(TextFieldValue()) }
//    val shopPhoneNumber = remember { mutableStateOf(TextFieldValue(shopPhoneNo.replaceFirst("+91","").trim())) }
    val shopPhoneNumber = remember { mutableStateOf(TextFieldValue("1234567890")) }
    val searchTag = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardBackgroundColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardBackgroundColorCode = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontStyle = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontResources = remember { mutableStateOf(Res.font.manrope_bold) }
    val shopBoardFontColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontColorCode = remember { mutableStateOf(TextFieldValue()) }
    val longitude = remember { mutableStateOf(TextFieldValue()) }
    val latitude = remember { mutableStateOf(TextFieldValue()) }
    val aboutShop = remember { mutableStateOf(TextFieldValue()) }
    // Error message state
    var errorMessage by remember { mutableStateOf("") }
    var showNumber by remember { mutableStateOf(false) }

    //data preferences
    val dataStore = DataStore.dataStore
    val myKey = stringPreferencesKey("userId")
    val preferencesFlow = dataStore.data
    val preferences by preferencesFlow.collectAsState(initial = emptyPreferences())
    val currentValue = preferences[myKey]?:"-1"

    if(currentValue!="-1"){
        println(currentValue)
    }

    shopBoardBackgroundColorName.value = TextFieldValue(
        viewModel.selectedBackGroundColorName.collectAsStateWithLifecycle().value ?: ""
    )
    shopBoardBackgroundColorCode.value = TextFieldValue(
        viewModel.selectedBackGroundColorCode.collectAsStateWithLifecycle().value ?: ""
    )

    shopBoardFontColorName.value = TextFieldValue(
        viewModel.selectedFontColorName.collectAsStateWithLifecycle().value ?: ""
    )

    shopBoardFontColorCode.value = TextFieldValue(
        viewModel.selectedFontColorCode.collectAsStateWithLifecycle().value ?: ""
    )

    val fontDetails = viewModel.selectedFonts.collectAsStateWithLifecycle().value

    if (fontDetails != null) {
        shopBoardFontStyle.value = TextFieldValue(fontDetails.first)
        shopBoardFontResources.value = fontDetails.second
    }

    var showShopAddressUI by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe response state from ViewModel
    val registerShopResponse by viewModel.registerShopResponse.collectAsStateWithLifecycle()
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }


    LaunchedEffect(Unit) {
        snapshotFlow { registerShopResponse }
            .collectLatest { response ->
                response?.let {
                    if (it.first) {
                        snackBarHostState.showSnackbar("Shop Registered Successfully")
                        viewModel.reset()
                        navController.popBackStack()
                    } else {
                        println(it.second)
                        snackBarHostState.showSnackbar(it.second)
                    }
                }
            }
    }


    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Shop Details",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.reset()
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
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .verticalScroll(
                            state = rememberScrollState(),
                            flingBehavior = ScrollableDefaults.flingBehavior()
                        )
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                keyboardController?.hide()
                            }
                        }
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Note: When adding shop make sure you will be in your shop as we are relocating your shop to serve you better.",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        fontSize = 15.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add Shop",
                        fontSize = 25.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextFieldWithLabel(label = "Shop Name", state = shopName)

                    TextFieldWithLabel(
                        label = "Shop Address",
                        maxLines = 3,
                        state = shopAddress,
                        onClick = {
//                            navController.navigate(Routes.SearchScreenPage)
                            showShopAddressUI = true
                        },
                        isEditable = false)

                    TextFieldWithLabel(
                        label = "Shop Address For Sign Board",
                        state = signBoardAddress
                    )

                    AutoCompleteTextField(
                        States.states.toList(),
                        state = shopState,
                        "Enter Your State"
                    )

                    TextFieldWithLabel(label = "Shop City", state = shopCity)


                    TextFieldWithLabel(label = "Shop Area", state = shopArea)

                    TextFieldWithLabel(
                        label = "Shop Phone Number",
                        state = shopPhoneNumber,
                        maxLength = 10,
                        inputType = KeyboardType.Number,
                        isEditable = false,
                        prefix = {
                            Text("+91", color = Color.Black)
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .border(
                                width = 0.5.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(2.dp)
                            ), // Optional padding for better spacing
                        verticalAlignment = Alignment.CenterVertically // Aligns content vertically
                    ) {
                        Text(
                            text = "Show Number",
                            modifier = Modifier.weight(1f)
                                .padding(start = 5.dp), // Makes the text take available space
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = showNumber,
                            onCheckedChange = { showNumber = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Yellow,
                                checkedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }

                    Text(
                        text = "Note:- you want search by multiple tags then add search tags separated by (comma)..",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    TextFieldWithLabel(label = "Search Tag", maxLines = 5, state = searchTag)

                    TextFieldWithLabel(
                        label = "Shop Board Font Style",
                        state = shopBoardFontStyle,
                        fontResources = shopBoardFontResources.value,
                        onClick = { navController.navigate(Routes.FontBottomSheet) },
                        isEditable = false
                    )

                    TextFieldWithLabel(
                        label = "Shop Board BackGround Color",
                        state = shopBoardBackgroundColorName,
                        onClick = {
                            navController.navigate(Routes.ColorBottomSheet)
                            viewModel.updateTheTypeOfColor("shop_board_color")
                        },
                        isEditable = false,
                        prefix = {
                            Card(modifier = Modifier
                                .size(24.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = CardDefaults.cardColors(containerColor = if (shopBoardBackgroundColorName.value.text.isNotEmpty()) Color.fromHex(
                                    shopBoardBackgroundColorCode.value.text
                                ) else Color.Black),
                                border = BorderStroke(0.5.dp, color = Color.Black)
                            ){

                            }
                        }
                    )

                    TextFieldWithLabel(
                        label = "Shop Font Color",
                        state = shopBoardFontColorName,
                        onClick = {
                            navController.navigate(Routes.ColorBottomSheet)
                            viewModel.updateTheTypeOfColor("shop_font_color")
                        },
                        isEditable = false,
                        prefix = {
                            Card(modifier = Modifier
                                .size(24.dp),
                                shape = RoundedCornerShape(2.dp),
                                colors = CardDefaults.cardColors(containerColor = if (shopBoardFontColorName.value.text.isNotEmpty()) Color.fromHex(
                                    shopBoardFontColorCode.value.text
                                ) else Color.Black),
                                border = BorderStroke(0.5.dp, color = Color.Black)
                            ){

                            }
                        }
                    )

                    TextFieldWithLabel(
                        label = "About Shop",
                        maxLines = 10,
                        state = aboutShop,
                        minLines = 3
                    )


                    if (isUploading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                errorMessage = ""
                                try {
                                    Util.validateShopAndSubmit(
                                        fields = mapOf(
                                            "Shop Name" to shopName.value.text,
                                            "Shop Address" to shopAddress.value.text,
                                            "Shop Address For Sign Board" to signBoardAddress.value.text,
                                            "Shop State" to shopState.value.text,
                                            "Shop City" to shopCity.value.text,
                                            "Shop Area" to shopArea.value.text,
                                            "Shop Phone Number" to shopPhoneNo,
                                            "Search Tag" to searchTag.value.text,
                                            "Shop Board Background" to shopBoardBackgroundColorCode.value.text,
                                            "Shop Board Font Colour" to shopBoardFontColorName.value.text,
                                            "Shop Board Font Style" to shopBoardFontStyle.value.text,
                                            "About Shop" to aboutShop.value.text,
                                            "Longitude" to longitude.value.text,
                                            "Latitude" to latitude.value.text
                                        ),
                                        setError = { error ->
                                            Util.showToast(error)
                                            errorMessage = error
                                        },
                                        status = { status ->
                                            if(status){
                                                viewModel.registerShop(
                                                    shopMaster = ShopMaster(
                                                        shopId = "",
                                                        userId = Objects.USER_ID,
                                                        shopName = shopName.value.text,
//                                                        shopMobileNo = shopPhoneNo,
                                                        shopMobileNo = shopPhoneNumber.value.text,
                                                        shopAddress = shopAddress.value.text,
                                                        shopAddress2 = signBoardAddress.value.text,
                                                        shopArea = shopArea.value.text,
                                                        city = shopCity.value.text,
                                                        state = shopState.value.text,
                                                        shopDescription = aboutShop.value.text,
                                                        bgColourId = shopBoardBackgroundColorCode.value.text,
                                                        fontSizeId = "",
                                                        fontStyleId = shopBoardFontStyle.value.text,
                                                        fontColourId = shopBoardFontColorCode.value.text,
                                                        isActive = "1",
                                                        flag = "1",
                                                        latitude = latitude.value.text,
                                                        longitude = longitude.value.text,
                                                        shopStatus = "",
                                                        searchTag = searchTag.value.text,
                                                        isVerified = "1",
                                                        createdAt = Util.getCurrentTimeStamp(),
                                                        updatedAt = Util.getCurrentTimeStamp(),
                                                        showNumber = if (showNumber) "0" else "1"
                                                    )
                                                )
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "An unexpected error occurred. Please try again."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Upload", fontSize = 20.sp)
                        }

                    }
                }
            }
        }
        if(showShopAddressUI){
            PlacesSearchScreen(
                addressDetails = { placeDetails ->
                    shopAddress.value = TextFieldValue(placeDetails.address)
                    latitude.value = TextFieldValue(placeDetails.latitude.toString())
                    longitude.value = TextFieldValue(placeDetails.longitude.toString())
                    shopArea.value = TextFieldValue(placeDetails.area)
                    shopCity.value = TextFieldValue(placeDetails.city)
                    shopState.value = TextFieldValue(placeDetails.state)
                }, onDismiss = {
                    showShopAddressUI = false
                }
            )
        }
    }
}

@Composable
fun TextFieldWithLabel(
    label: String,
    maxLines: Int = 1,
    fontResources: FontResource = Res.font.manrope_medium,
    state: MutableState<TextFieldValue>,
    isEditable: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
    inputType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (TextFieldValue) -> Unit = {},
    color: Color = Color.Black,
    onClick: () -> Unit = {},
    prefix: (@Composable () -> Unit)? = null // Optional suffix
) {

    OutlinedTextField(
        value = state.value,
        label = { Text(text = label, color = Color.Gray) },
        onValueChange = { newText ->
            if (inputType == KeyboardType.Number) {
                if (newText.text.all { it.isDigit() } && newText.text.length <= maxLength) {
                    state.value = newText
                }
            } else {
                if (newText.text.length <= maxLength) {
                    state.value = newText
                }
            }
            onValueChange(newText)
        },
        modifier = modifier
            .background(Color.White, MaterialTheme.shapes.small)
            .padding(8.dp)
            .clickable { onClick() },
        textStyle = TextStyle(
            color = color,
            fontSize = 15.sp,
            fontFamily = FontFamily(Font(fontResources)),
            lineHeight = 15.sp
        ),
        enabled = isEditable,
        maxLines = maxLines,
        minLines = minLines,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = inputType
        ),
        readOnly = !isEditable,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Black,  // Black when focused
            unfocusedIndicatorColor = Color.Black, // Black when not focused
            disabledIndicatorColor = Color.Black, // Black when disabled
            errorIndicatorColor = Color.Red // Optional: Red when there's an error
        ),
        prefix = {
            if (prefix != null) {
                Row(
                    modifier = Modifier.padding(end = 8.dp) // Add space between prefix and text
                ) {
                    prefix()
                }
            }
        }
    )
}

@Composable
fun AutoCompleteTextField(
    suggestions: List<String>,
    state: MutableState<TextFieldValue>,
    hintText: String
) {
    var filteredSuggestions by remember { mutableStateOf(suggestions) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        OutlinedTextField(
            value = state.value,
            onValueChange = { newValue ->
                if (newValue.text != state.value.text) {
                    state.value = newValue
                    showSuggestions = newValue.text.isNotEmpty()

                    filteredSuggestions = if (newValue.text.isEmpty()) {
                        suggestions
                    } else {
                        suggestions.filter {
                            it.contains(newValue.text, ignoreCase = true)
                        }
                    }
                }
            },
            label = {
                Text(
                    text = hintText,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, MaterialTheme.shapes.small)
                .padding(8.dp)
                .focusRequester(focusRequester),
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showSuggestions) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp) // Constrain height of suggestions list
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(bottom = 8.dp)
            ) {
                if (filteredSuggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                            .background(color = Color.White)
                            .animateContentSize()
                    ) {
                        items(
                            items = filteredSuggestions,
                            key = { suggestion -> suggestion }
                        ) { suggestion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        state.value = TextFieldValue(
                                            text = suggestion,
                                            selection = TextRange(suggestion.length)
                                        )
                                        showSuggestions = false
                                    }
                                    .background(color = Color.White)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    style = TextStyle(fontSize = 18.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No suggestions available",
                        style = TextStyle(color = Color.Gray, fontSize = 16.sp),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
