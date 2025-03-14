package com.pepdeal.infotech.shop.editShop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.fonts.FontItemCard
import com.pepdeal.infotech.fonts.FontUtils
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.ViewModals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopFontBottomSheet(
    viewModal: EditShopDetailsViewModal = ViewModals.editShopViewModal,
    onDismiss: () -> Unit
) {

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
                    text = "Shop Board Font Style",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Divider Line
            HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 5.dp))

            // RecyclerView Equivalent: LazyColumn
            LazyColumn {
                items(items = FontUtils.fontList.distinctBy { it.fontFamily },
                    key = {it.fontFamily}) { (fontName, fontResource) ->
                    FontItemCard(fontName, fontResource){ _fontName,_fontResource ->
                        viewModal.updateTheShopFontFamily(_fontName,_fontResource)
                        NavigationProvider.navController.popBackStack()
                    }
                }
            }
        }
    }
}