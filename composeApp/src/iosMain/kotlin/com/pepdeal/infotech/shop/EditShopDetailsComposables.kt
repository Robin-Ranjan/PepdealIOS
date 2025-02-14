package com.pepdeal.infotech.shop

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.fonts.FontUtils
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.ViewModals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.manrope_regular

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditShopDetailsScreen(
    shopId: String,
    viewModal: EditShopDetailsViewModal = ViewModals.editShopViewModal
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val shopDetails by viewModal.shopDetails.collectAsStateWithLifecycle()
    val uploading by viewModal.shopLoading.collectAsStateWithLifecycle()
    val shopServices by viewModal.shopServices.collectAsStateWithLifecycle()

    val shopBoardBackgroundColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardBackgroundColorCode = remember { mutableStateOf(TextFieldValue()) }

    val aboutShop = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontStyle = remember { mutableStateOf(TextFieldValue()) }
    var showNumber by remember { mutableStateOf(false) }
    val shopBoardFontResources = remember { mutableStateOf(TextFieldValue()) }

    val shopBoardFontColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontColorCode = remember { mutableStateOf(TextFieldValue()) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(shopId) {
        viewModal.fetchShopDetails(shopId)
        viewModal.fetchShopServices(shopId)
    }

    LaunchedEffect(shopDetails) {
        shopBoardBackgroundColorCode.value = TextFieldValue(shopDetails.bgColourId ?: "")
        shopBoardBackgroundColorName.value = TextFieldValue(ColorUtil.colorMap.entries.find { it.value == shopBoardBackgroundColorCode.value.text }?.key?:"Default")
        shopBoardFontColorCode.value = TextFieldValue(shopDetails.fontColourId)
        shopBoardFontColorName.value = TextFieldValue(ColorUtil.colorMap.entries.find { it.value == shopBoardFontColorCode.value.text }?.key?:"Default")
        aboutShop.value = TextFieldValue(shopDetails.shopDescription ?: "")
        shopBoardFontResources.value = TextFieldValue(shopDetails.fontStyleId ?: "")
        showNumber = shopDetails.showNumber == "0"
    }

    // Use derived state to update when shopServices changes
    val serviceOptions = remember(shopServices) {
        listOf(
            "Cash on Delivery" to mutableStateOf(shopServices.cashOnDelivery == "0"),
            "Door Step" to mutableStateOf(shopServices.doorStep == "0"),
            "Home Delivery" to mutableStateOf(shopServices.homeDelivery == "0"),
            "Live Demo" to mutableStateOf(shopServices.liveDemo == "0"),
            "Offers" to mutableStateOf(shopServices.offers == "0"),
            "Bargain" to mutableStateOf(shopServices.bargain == "0")
        )
    }

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Edit Shop Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
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
                    Column (modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.fromHex(shopDetails.bgColourId?:""))
                                .padding(5.dp)
                        ) {
                            Text(
                                text = shopDetails.shopName ?: "",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 18.sp
                                ),
                                color = Color.fromHex(shopDetails.fontColourId),
                                modifier = Modifier
                                    .fillMaxWidth() // Makes the Text fill the available width
                                    .padding(top = 5.dp),
                                textAlign = TextAlign.Center,// Centers the text within the available width
                            )

                            // Shop Address
                            Text(
                                text = shopDetails.shopAddress2 ?: "",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 12.sp
                                ),
                                color = Color.fromHex(shopDetails.fontColourId),
                                modifier = Modifier
                                    .fillMaxWidth(), // Makes the Text fill the available width
                                textAlign = TextAlign.Center // Centers the text within the available width
                            )
                        }

                        TextFieldWithLabel(
                            label = "Shop Board BackGround Color",
                            state = shopBoardBackgroundColorName,
                            onClick = {
                                navController.navigate(Routes.ColorBottomSheet)
                                viewModal.updateTheTypeOfColor("shop_board_color")
                            },
                            isEditable = false,
                            color = if (shopBoardBackgroundColorCode.value.text.isNotEmpty()) Color.fromHex(
                                shopBoardBackgroundColorCode.value.text
                            ) else Color.Black
                        )

                        TextFieldWithLabel(
                            label = "Shop Board Font Style",
                            state = shopBoardFontResources,
                            fontResources = FontUtils.getFontResourceByName(shopBoardFontResources.value.text)?:Res.font.manrope_medium,
                            onClick = { navController.navigate(Routes.FontBottomSheet) },
                            isEditable = false
                        )

                        TextFieldWithLabel(
                            label = "Shop Font Color",
                            state = shopBoardFontColorName,
                            onClick = {
                                navController.navigate(Routes.ColorBottomSheet)
                                viewModal.updateTheTypeOfColor("shop_font_color")
                            },
                            isEditable = false,
                            color = if (shopBoardFontColorCode.value.text.isNotEmpty()) Color.fromHex(
                                shopBoardFontColorCode.value.text
                            ) else Color.Black
                        )

                        TextFieldWithLabel(
                            label = "About Shop",
                            maxLines = 10,
                            state = aboutShop,
                            minLines = 3
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 20.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White)
                                .border(0.5.dp, Color.Gray, RoundedCornerShape(5.dp))
                        ) {
                            var isExpanded by remember { mutableStateOf(false) }

                            // Title Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded
                                        if (isExpanded) {
                                            scope.launch {
                                                delay(200) // Wait for animation
                                                bringIntoViewRequester.bringIntoView()
                                            }
                                        }}
                                    .padding(vertical = 10.dp, horizontal = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Shop Services",
                                    fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand/Collapse"
                                )
                            }

                            if (isExpanded) {
                                HorizontalDivider(color = Color.Gray, thickness = 2.dp)

                                Column(
                                    modifier = Modifier.padding(5.dp)
                                        .bringIntoViewRequester(bringIntoViewRequester)
                                ) {
                                    serviceOptions.forEach { (label, state) ->
                                        ServiceSwitch(label, state)
                                    }
                                }
                            }
                        }

                        if(uploading){
                            CircularProgressIndicator()
                        }else{
                            Button(modifier = Modifier.fillMaxWidth(), onClick = {

                            }){
                                Text(text = "Update",fontSize = 20.sp)
                            }
                        }

                    }
                }
            }
        }
    }

}


// Reusable Service Switch Component
@Composable
fun ServiceSwitch(label: String, state: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = FontFamily(Font(Res.font.manrope_regular)),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = state.value,
            onCheckedChange = { state.value = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6A1B9A), // Example Secondary Color
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
    HorizontalDivider(color = Color.Gray, thickness = 2.dp)
}