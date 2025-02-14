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
import androidx.compose.material3.ButtonDefaults
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
import com.pepdeal.infotech.ShopMaster
import com.pepdeal.infotech.fonts.FontUtils
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.NavigationProvider.navController
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.Util.toNameFormat
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
    userId: String,
    viewModal: EditShopDetailsViewModal = ViewModals.editShopViewModal
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    // Collect ViewModel states efficiently
    val shopDetails by viewModal.shopDetails.collectAsStateWithLifecycle()
    val isUploaded by viewModal.isUploaded.collectAsStateWithLifecycle()
    val shopServices by viewModal.shopServices.collectAsStateWithLifecycle()

    val backgroundColorCode by viewModal.selectedBackGroundColorCode.collectAsStateWithLifecycle("")
    val backgroundColorName by viewModal.selectedBackGroundColorName.collectAsStateWithLifecycle("")
    val fontColorCode by viewModal.selectedFontColorCode.collectAsStateWithLifecycle("")
    val fontColorName by viewModal.selectedFontColorName.collectAsStateWithLifecycle("")
    val fontResource by viewModal.selectedFonts.collectAsStateWithLifecycle(null)

    // UI state holders (updated when `shopDetails` changes)
    val shopBoardBackgroundColorCode = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardBackgroundColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontColorCode = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontColorName = remember { mutableStateOf(TextFieldValue()) }
    val shopBoardFontResources = remember { mutableStateOf(TextFieldValue()) }
    val aboutShop = remember { mutableStateOf(TextFieldValue()) }
    var showNumber by remember { mutableStateOf(false) }

    // Fetch shop details on screen load
    LaunchedEffect(shopId) {
        viewModal.fetchShopDetails(shopId)
        viewModal.fetchShopServices(shopId)
    }

    // Update UI state when shop details change
    LaunchedEffect(shopDetails) {
        aboutShop.value = TextFieldValue(shopDetails.shopDescription ?: "")
        showNumber = shopDetails.showNumber == "0"
    }

    // Update UI state when colors and fonts change
    LaunchedEffect(
        backgroundColorCode,
        backgroundColorName,
        fontColorCode,
        fontColorName,
        fontResource
    ) {
        shopBoardBackgroundColorCode.value = TextFieldValue(backgroundColorCode ?: "")
        shopBoardBackgroundColorName.value = TextFieldValue(backgroundColorName ?: "")
        shopBoardFontColorCode.value = TextFieldValue(fontColorCode ?: "")
        shopBoardFontColorName.value = TextFieldValue(fontColorName ?: "")
        shopBoardFontResources.value = TextFieldValue(fontResource?.first ?: "")
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

    // Function to update shop services when user toggles an option
    fun updateShopServices() {
        val updatedShopStatus = shopServices.copy(
            shopStatusId = "",
            userId = userId,
            shopId = shopId,
            cashOnDelivery = if (serviceOptions[0].second.value) "0" else "1",
            doorStep = if (serviceOptions[1].second.value) "0" else "1",
            homeDelivery = if (serviceOptions[2].second.value) "0" else "1",
            liveDemo = if (serviceOptions[3].second.value) "0" else "1",
            offers = if (serviceOptions[4].second.value) "0" else "1",
            bargain = if (serviceOptions[5].second.value) "0" else "1",
            updatedAt = Util.getCurrentTimeStamp(), // Ensure timestamp is updated
            createdAt = Util.getCurrentTimeStamp()
        )

        val shopMaster = shopDetails.copy(
            bgColourId = shopBoardBackgroundColorCode.value.text,
            fontStyleId = shopBoardFontResources.value.text,
            fontColourId = shopBoardFontColorCode.value.text,
            shopDescription = aboutShop.value.text.trim(),
            showNumber = if (showNumber) "0" else "1",
            updatedAt = Util.getCurrentTimeStamp()
        )


        viewModal.updateShopDetails(updatedShopStatus,shopMaster)
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
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.fromHex(shopBoardBackgroundColorCode.value.text))
                                .padding(5.dp)
                        ) {
                            Text(
                                text = shopDetails.shopName?.toNameFormat() ?: "",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(
                                        Font(
                                            FontUtils.getFontResourceByName(shopBoardFontResources.value.text)
                                                ?: Res.font.manrope_medium
                                        )
                                    ),
                                    lineHeight = 18.sp
                                ),
                                color = Color.fromHex(shopBoardFontColorCode.value.text),
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
                                color = Color.fromHex(shopBoardFontColorCode.value.text),
                                modifier = Modifier
                                    .fillMaxWidth(), // Makes the Text fill the available width
                                textAlign = TextAlign.Center // Centers the text within the available width
                            )
                        }

                        TextFieldWithLabel(
                            label = "Shop Board BackGround Color",
                            state = shopBoardBackgroundColorName,
                            onClick = {
                                navController.navigate(Routes.EditShopColorBottomSheet)
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
                            fontResources = FontUtils.getFontResourceByName(shopBoardFontResources.value.text)
                                ?: Res.font.manrope_medium,
                            onClick = { navController.navigate(Routes.EditShopFontBottomSheet) },
                            isEditable = false
                        )

                        TextFieldWithLabel(
                            label = "Shop Font Color",
                            state = shopBoardFontColorName,
                            onClick = {
                                navController.navigate(Routes.EditShopColorBottomSheet)
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
                                    .clickable {
                                        isExpanded = !isExpanded
                                        if (isExpanded) {
                                            scope.launch {
                                                delay(200) // Wait for animation
                                                bringIntoViewRequester.bringIntoView()
                                            }
                                        }
                                    }
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

                        if (isUploaded) {
                            CircularProgressIndicator()
                        } else {
                            Button(modifier = Modifier.fillMaxWidth().padding(5.dp),
                                colors = ButtonDefaults.buttonColors(Color.Black), onClick = {
                                    updateShopServices()
                                }) {
                                Text(text = "Update", fontSize = 20.sp)
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