package com.pepdeal.infotech.categories

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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.fonts.FontItemCard
import com.pepdeal.infotech.fonts.FontUtils
import com.pepdeal.infotech.product.ProductViewModal
import com.pepdeal.infotech.util.CategoriesUtil
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.ViewModals
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesBottomSheet(
    onDismiss: () -> Unit,
    viewModal: ProductViewModal = ViewModals.productViewModal
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
                    text = "Product Categories",
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
                items(items = CategoriesUtil.productCategories.distinctBy { it.id },
                    key = { it.id }) { category ->
                    CategoriesItemCard(category) { _categories, ->
                        viewModal.updateProductCategories(_categories)
                        NavigationProvider.navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesItemCard(categories: ProductCategories, onCategoriesSelected: (ProductCategories) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoriesSelected(categories) }
            .padding(5.dp),
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = categories.name.toCharArray()[0].toString(),
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = categories.name.toNameFormat(), fontSize = 18.sp, fontFamily = FontFamily(
                    Font(Res.font.manrope_bold)
                )
            )
        }
    }
}