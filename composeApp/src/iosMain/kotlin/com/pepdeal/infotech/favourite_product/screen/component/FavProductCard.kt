package com.pepdeal.infotech.favourite_product.screen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.favourite_product.modal.FavProductWithImages
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.toRupee
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform
import pepdealios.composeapp.generated.resources.place_holder
import pepdealios.composeapp.generated.resources.red_heart


@Composable
fun FavoriteProductCard(
    product: FavProductWithImages,
    onDeleteClick: (String) -> Unit,
    onFavClick: (String) -> Unit
) {
    val productDetails = product.product
    val productImage = product.images[0].productImages

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onFavClick(productDetails.productId) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                CoilImage(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(width = 1.dp, color = Color.Gray),
                    imageModel = { productImage },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    previewPlaceholder = painterResource(Res.drawable.compose_multiplatform),
                    loading = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    },
                    failure = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder), // Show a default placeholder on failure
                            contentDescription = "Placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Column for Product Details
                Column(
                    modifier = Modifier
                        .weight(1f) // Takes up all available space
                        .padding(end = 40.dp) // Ensures icon does not overlap text
                ) {
                    Text(
                        text = productDetails.productName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2, // Allows wrapping to second line if needed
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    if (productDetails.onCall == "1") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = productDetails.mrp.toRupee(),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = productDetails.sellingPrice.toRupee(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (productDetails.discountMrp.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${productDetails.discountMrp}% off",
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "On Call",
                            fontSize = 14.sp,
                            color = Color.Black,
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        Text(
                            text = "Date: ",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = Util.formatDateWithTimestamp(product.createdAt),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Delete Icon (Top-Right Corner)
            IconButton(
                onClick = { onDeleteClick(productDetails.productId) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.red_heart),
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}