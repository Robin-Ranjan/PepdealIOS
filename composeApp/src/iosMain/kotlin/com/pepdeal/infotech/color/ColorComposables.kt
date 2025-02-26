package com.pepdeal.infotech.color

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.product.addProduct.AddNewProductViewModal
import com.pepdeal.infotech.shop.OpenYourShopViewModal
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.fromHex
import com.pepdeal.infotech.util.ViewModals


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorBottomSheet(
    viewModal: OpenYourShopViewModal = ViewModals.openYOurShopViewModal,
    onDismiss: () -> Unit
) {
    val forColorScreen by viewModal.forScreen.collectAsStateWithLifecycle()

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight()
            .background(color = Color.White)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        sheetState = rememberModalBottomSheetState(true),
        containerColor = Color.White,
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Row with Close Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop Board Background",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Divider Line
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 5.dp)
            )

            // RecyclerView Equivalent: LazyColumn
            LazyColumn {
                items(items = ColorUtil.colorList.distinctBy { it.hexCode },
                    key = { it.hexCode }) { (colorName, colorCode) ->
                    ColorItemCard(colorName, colorCode, onColorClick = { _colorName, _colorCode ->
                        if (forColorScreen != null && forColorScreen!!.isNotEmpty()) {
                            if (forColorScreen!! == "shop_board_color") {
                                println(_colorCode)
                                viewModal.updateSelectedBackgroundColor(_colorName, _colorCode)
                            } else {
                                viewModal.updateSelectedFontColor(_colorName, _colorCode)
                            }
                            NavigationProvider.navController.popBackStack()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun ColorItemCard(colorName: String, colorCode: String, onColorClick: (String, String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable { onColorClick(colorName, colorCode) },
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .size(24.dp),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(0.5.dp, Color.Black),
                colors = CardDefaults.cardColors(containerColor = Color.fromHex(colorCode))
            ) {}

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = colorName, fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleColorBottomSheet(
    viewModal: AddNewProductViewModal = ViewModals.addNewProductViewModal,
    onDismiss: () -> Unit
) {

    ColorUtil.colorList.forEach { it.isSelected = false }
    val colorList = ColorUtil.colorList.toMutableList() // Copy if needed

    val selectedColors = remember { mutableStateListOf<ColorItem>() }
    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight()
            .background(color = Color.White)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        sheetState = rememberModalBottomSheetState(true),
        containerColor = Color.White,
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Row with Close Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop Board Background",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Divider Line
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 5.dp)
            )

            // RecyclerView Equivalent: LazyColumn
            LazyColumn (
                modifier = Modifier.weight(1f)
            ){
                items(items = colorList.distinctBy { it.hexCode },
                    key = { it.hexCode }) { color ->
                    MultiColorColorItemCard(
                        color = color,
                        isSelected = selectedColors.contains(color),
                        onSelectionChange = { isSelected ->
                            if (isSelected) {
                                selectedColors.add(color) // Add to selected
                            } else {
                                selectedColors.remove(color) // Remove from selected
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    viewModal.updateProductColours(selectedColors)
                    onDismiss()
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

@Composable
fun MultiColorColorItemCard(color: ColorItem, isSelected: Boolean, onSelectionChange: (Boolean) -> Unit) {
    // Use remember to track the state of 'isSelected' for this card
//    var isSelected by remember { mutableStateOf(color.isSelected) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {  onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, if(isSelected)Color.Green else Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Ensures elements are spaced properly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(0.5.dp, Color.Black),
                    colors = CardDefaults.cardColors(containerColor = Color.fromHex(color.hexCode))
                ) {}

                Spacer(modifier = Modifier.width(10.dp))

                Text(text = color.name, fontSize = 18.sp)
            }

            // Show the checkmark icon if isSelected is true
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

}