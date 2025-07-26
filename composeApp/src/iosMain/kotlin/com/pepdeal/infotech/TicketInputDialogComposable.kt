package com.pepdeal.infotech

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.tickets.model.TicketMaster
import com.pepdeal.infotech.util.ColorUtil
import com.pepdeal.infotech.util.Util

@Composable
fun TicketDialog(
    productDetails: ProductMaster,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,// Size, Color, Quantity,
    onSubmitTicket: (TicketMaster) -> Unit
) {

    val colorList: SnapshotStateList<String> =
        if (productDetails.color.isNotEmpty() && productDetails.color.isNotBlank()) {
            productDetails.color.split(",").toList()
                .mapNotNull { code -> ColorUtil.colorMap.entries.find { it.value == code }?.key }
                .filter { it.isNotBlank() && it != "None" }.toMutableStateList()
        } else {
            mutableStateListOf()
        }


    val sizeList: SnapshotStateList<String> =
        if (productDetails.sizeId.isNotEmpty() && productDetails.sizeId.isNotBlank()) {
            productDetails.sizeId.split(",").toMutableStateList()
        } else {
            mutableStateListOf()
        }
    if (showDialog) {

        AnimatedVisibility(
            visible = showDialog,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                initialScale = 0.5f,
                animationSpec = tween(500)
            ),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            AlertDialog(
                onDismissRequest = onDismiss,
                modifier = Modifier.padding(5.dp),
                containerColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                text = {
                    Column(
                        modifier = Modifier.padding(0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Generate Ticket",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            color = Color.DarkGray,
                            thickness = 3.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )

                        var selectedSize by remember { mutableStateOf("") }
                        var selectedColor by remember { mutableStateOf("") }
                        var quantity by remember { mutableStateOf("") }

                        if (sizeList.isNotEmpty()) {
                            DropdownMenuComponent(
                                label = "Select Size",
//                            options = listOf("S", "M", "L", "XL")
                                options = sizeList
                            ) {
                                selectedSize = it
                            }
                        }

                        if (colorList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            DropdownMenuComponent(
                                label = "Select Color",
                                options = colorList
                            ) {
                                selectedColor = it
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Enter Quantity", color = Color.LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                modifier = Modifier.weight(1f)
                                    .padding(start = 3.dp, end = 3.dp, top = 3.dp),
                                shape = RoundedCornerShape(5.dp),
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Text(text = "Cancel", color = Color.Black)
                            }

                            Button(
                                modifier = Modifier.weight(1f)
                                    .padding(start = 3.dp, end = 3.dp, top = 3.dp),
                                shape = RoundedCornerShape(5.dp),
                                onClick = {
                                    onSubmit(selectedSize, selectedColor, quantity)

                                    val ticket = TicketMaster(
                                        ticketId = "",
                                        userId = "",
                                        shopId = productDetails.shopId,
                                        productId = productDetails.productId,
                                        ticketStatus = "2",
                                        sellingPrice = if (productDetails.onCall == "1") productDetails.sellingPrice else "On Call",
                                        colour = selectedColor,
                                        sizeName = selectedSize,
                                        quantity = quantity,
                                        createdAt = Util.getCurrentTimeStamp(),
                                        updatedAt = Util.getCurrentTimeStamp()
                                    )
                                    onSubmitTicket(ticket)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFFF5722
                                    )
                                ), // Replace with app color
                                border = BorderStroke(0.5.dp, Color.Black)
                            ) {
                                Text(text = "Submit", color = Color.Black)
                            }
                        }
                    }
                },
                confirmButton = {},
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = true
                )
            )
        }
    }
}

@Composable
fun DropdownMenuComponent(label: String, options: List<String>, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(label) }
    var menuWidth by remember { mutableStateOf(0.dp) } // To store the width of the parent Box
    val localDensity = LocalDensity.current
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .border(1.dp, Color.Gray, RoundedCornerShape(5.dp)).clickable { expanded = true }
            .onGloballyPositioned { layoutCoordinates ->
                // Capture the width of the parent Box
                menuWidth = with(localDensity) { layoutCoordinates.size.width.toDp() }
            }) {
        Text(
            text = selectedOption,
            modifier = Modifier.padding(16.dp),
            color = if (selectedOption == label) Color.Gray else Color.Black
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidth)
                .heightIn(max = 250.dp),
            containerColor = Color.White
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        onSelection(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                        .background(color = Color.White),
                )
            }
        }
    }
}
