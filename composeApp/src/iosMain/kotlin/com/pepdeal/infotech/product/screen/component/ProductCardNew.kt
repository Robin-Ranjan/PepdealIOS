package com.pepdeal.infotech.product.screen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.shop.BackGroundColor
import com.pepdeal.infotech.util.Util.toDiscountFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_regular
import pepdealios.composeapp.generated.resources.place_holder

@Composable
fun ProductCardNew(
    shopItems: ShopItems,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit,
    onProductClicked: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onProductClicked() }
            )
        ) {
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                // Left: Product Image
                CoilImage(
                    imageModel = { shopItems.image },
                    modifier = Modifier
                        .width(140.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackGroundColor),
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.FillHeight,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                            setToSaturation(1f)
                        })
                    ),
                    previewPlaceholder = painterResource(Res.drawable.place_holder),
                    loading = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    failure = {
                        Image(
                            painter = painterResource(Res.drawable.place_holder),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Right: Product Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = shopItems.productName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(end = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (shopItems.onCall == "1") {
                            Text(
                                text = shopItems.sellingPrice.toTwoDecimalPlaces().toRupee(),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = shopItems.mrp.toTwoDecimalPlaces().toRupee(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${shopItems.discountMrp.toDiscountFormat()})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(
                                        0xFF388E3C
                                    )
                                )
                            )
                        } else {
                            Text(
                                text = "On Call",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = shopItems.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.DarkGray,
                            fontFamily = FontFamily(Font(Res.font.manrope_regular))
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            println(isFavorite)
            FavoriteToggleIcon(
                onToggle = { onFavoriteClick() },
                isFavorite = isFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(start = 10.dp, top = 3.dp)
            )
        }

        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            1.dp,
            Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun FavoriteToggleIcon(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
//    var isFlipping by remember { mutableStateOf(false) }
//    var pendingFavoriteState by remember { mutableStateOf(isFavorite) }
//
//    val animatedRotation by animateFloatAsState(
//        targetValue = if (isFlipping) 180f else 0f,
//        animationSpec = tween(durationMillis = 400),
//        label = "RotationY"
//    )
//
//    // When animation finishes, toggle state and call callback
//    LaunchedEffect(animatedRotation) {
//        if (animatedRotation == 180f) {
//            // Update temporary state to flip icon immediately
//            pendingFavoriteState = !pendingFavoriteState
//            onToggle()
//            isFlipping = false
//        }
//    }

    IconButton(
        onClick = {
//            if (!isFlipping) {
//                isFlipping = true
//            }
            onToggle()
        },
        modifier = modifier
            .graphicsLayer {
//                rotationY = animatedRotation
//                cameraDistance = 12 * density
            }
    ) {
//        val showFilled = if (isFlipping) !pendingFavoriteState else pendingFavoriteState

        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color.Red else Color.Gray
        )
    }
}


fun calculateSavings(mrp: Double, sellingPrice: Double): Double {
    return (mrp - sellingPrice).coerceAtLeast(0.0)
}