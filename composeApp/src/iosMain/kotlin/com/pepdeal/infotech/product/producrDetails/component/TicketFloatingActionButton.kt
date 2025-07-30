package com.pepdeal.infotech.product.producrDetails.component

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TicketFloatingActionButton(
    isTicketExists: Boolean,
    onClick: () -> Unit,
) {
    val buttonColor = if (isTicketExists) Color.Gray else Color.White
    val iconColor = if (isTicketExists) Color.LightGray else Color.Black

    FloatingActionButton(
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        containerColor = buttonColor,
        elevation = FloatingActionButtonDefaults.elevation(5.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Floating Button",
            tint = iconColor
        )
    }
}