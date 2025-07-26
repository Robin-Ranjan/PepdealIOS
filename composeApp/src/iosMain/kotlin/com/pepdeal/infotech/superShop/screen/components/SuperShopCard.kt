package com.pepdeal.infotech.superShop.screen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.fonts.FontUtils.getFontResourceByName
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.ShopItemView
import com.pepdeal.infotech.superShop.model.SuperShopsWithProduct
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.fromHex
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.super_shop_positive

@Composable
fun SuperShopCardView(
    superShopWithProduct: SuperShopsWithProduct,
    onDeleteClick: (String) -> Unit,
    onShopClicked: (String) -> Unit
) {
    val cardBackgroundColor = Color.fromHex(superShopWithProduct.shop.bgColourId ?: "#FFFFFF")
    val shopNameColor = Color.fromHex(superShopWithProduct.shop.fontColourId)
    val fontResource: FontResource =
        getFontResourceByName(superShopWithProduct.shop.fontStyleId ?: "") ?: Res.font.manrope_bold
    val customFont = FontFamily(Font(fontResource))

    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackgroundColor)
                    .clickable { superShopWithProduct.shop.shopId?.let { onShopClicked(it) } }
                    .padding(5.dp)
            ) {
                // Shop Name and Address
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        text = superShopWithProduct.shop.shopName.orEmpty(),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = customFont
                        ),
                        color = shopNameColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = superShopWithProduct.shop.shopAddress2 ?: "",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = shopNameColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Icons (Call & Delete)
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { Util.openDialer("") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.Black
                        )
                    }
                    IconButton(
                        onClick = { onDeleteClick(superShopWithProduct.shop.shopId ?: "-1") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.super_shop_positive),
                            contentDescription = "Delete",
                            tint = Color.Black
                        )
                    }
                }
            }

            // Products List (LazyRow)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(
                    items = superShopWithProduct.products,
                    key = { it.product.productId }
                ) { shopItem ->
                    ShopItemView(shopItem) {
                        NavigationProvider.navController.navigate(Routes.ProductDetailsPage(it))
                    }
                }
            }
        }
    }
}