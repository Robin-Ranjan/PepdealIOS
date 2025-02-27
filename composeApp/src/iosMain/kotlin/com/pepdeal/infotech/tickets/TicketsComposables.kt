package com.pepdeal.infotech.tickets

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerTicketScreen(
    userId: String,
    viewModal: TicketViewModal = ViewModals.customerTicketViewModal
) {
    val ticketProductList by viewModal.ticketProduct.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (ticketProductList.isEmpty()) {
            viewModal.getAllTicketProduct(userId)
        }
    }

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tickets",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModal.resetTicket()
                                NavigationProvider.navController.popBackStack()
                            }
                        ) {
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

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            items(items = ticketProductList,
                                key = { it.ticket.ticketId }) { ticketProduct ->
                                TicketProductCard(
                                    ticketProduct = ticketProduct,
                                    onTicketClicked = { productId ->
                                        NavigationProvider.navController.navigate(
                                            Routes.ProductDetailsPage(
                                                productId
                                            )
                                        )
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketProductCard(
    ticketProduct: ProductTicket,
    onTicketClicked: (String) -> Unit
) {
    val ticketDetails = ticketProduct.ticket
    val productImage = ticketProduct.imageUrl

    val (statusText, color) = when (ticketDetails.ticketStatus) {
        "0" -> "Approved" to Color.Green
        "1" -> "Rejected" to Color.Red
        "2" -> "Waiting" to Color.Gray
        "3" -> "Delivered" to Color.Blue
        else -> "Unknown" to Color.Black
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTicketClicked(ticketDetails.productId) }
            .padding(5.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                Card(
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        previewPlaceholder = painterResource(Res.drawable.compose_multiplatform)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    // Product Name
                    Text(
                        text = ticketProduct.productName,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Price & Discount Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (ticketDetails.sellingPrice != "-1") "₹${ticketDetails.sellingPrice}" else "On Call",
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "QTY:- ${ticketDetails.quantity}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Date: ${Util.formatDateWithTimestamp(ticketDetails.updatedAt)}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
            }
            // Status Text
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)  // Define a fixed size
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = color,
                    modifier = Modifier
                        .padding(5.dp),
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
