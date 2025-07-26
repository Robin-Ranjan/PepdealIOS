package com.pepdeal.infotech.product.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold
import pepdealios.composeapp.generated.resources.manrope_medium
import pepdealios.composeapp.generated.resources.manrope_semibold
import pepdealios.composeapp.generated.resources.place_holder

@Composable
fun ProductCard(
    shopItems: ShopItems,
    isFavorite: Boolean,
    onLikeClicked: () -> Unit,
    onProductClicked: (ShopItems) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .clickable { onProductClicked(shopItems) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(3.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(2.dp),
                    elevation = CardDefaults.elevatedCardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        imageModel = { shopItems.image },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                setToSaturation(1f)
                            })
                        ),
                        previewPlaceholder = painterResource(Res.drawable.place_holder),
                        loading = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop,
                            )
                        },
                        failure = {
                            Image(
                                painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                                contentDescription = "Placeholder",
                                modifier = Modifier.fillMaxSize()
                                    .background(color = Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                }

                // Discount Badge (Top-Left)
                if (shopItems.discountMrp.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                Color(0xFFFF9800).copy(alpha = 0.7f),
                                shape = RoundedCornerShape(bottomEnd = 8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = shopItems.discountMrp.toDiscountFormat(),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onLikeClicked()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(30.dp),
                        tint = if (isFavorite) Color.Red else Color.Gray,
                    )
                }
            }

            Text(
                text = shopItems.productName.toNameFormat(),
                fontSize = 13.sp,
                lineHeight = 13.sp,
                color = Color.Black,
                maxLines = 1,
                fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth()
            )

            if (shopItems.onCall == "1") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = shopItems.sellingPrice.toTwoDecimalPlaces().toRupee(),
                        fontFamily = FontFamily(Font(Res.font.manrope_semibold)),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = Color.Black
                    )

                    Text(
                        text = shopItems.mrp.toTwoDecimalPlaces().toRupee(),
                        fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(horizontal = 5.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            } else {
                Text(
                    text = "On Call",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontFamily = FontFamily(Font(Res.font.manrope_medium)),
                    color = Color.Red,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}
