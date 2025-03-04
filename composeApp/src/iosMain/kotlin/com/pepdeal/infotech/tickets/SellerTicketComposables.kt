package com.pepdeal.infotech.tickets

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.CircleCropShape
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.toNameFormat
import com.pepdeal.infotech.util.Util.toRupee
import com.pepdeal.infotech.util.Util.toTwoDecimalPlaces
import com.pepdeal.infotech.util.ViewModals
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.compose_multiplatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerTicketScreen(viewModal: SellerTicketViewModal = ViewModals.sellerTicketViewModal) {

    val sellerTicketProductList by viewModal.sellerTicketProduct.collectAsStateWithLifecycle()
    val isLoading by viewModal.isLoading.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if(sellerTicketProductList.isEmpty()){
            viewModal.getAllSellerTicketProduct("-OIssBIQoj5chr2PuTLo")
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
                        ) {
                            items(items = sellerTicketProductList) { tickets ->
                                SellerTicketCard(
                                    item = tickets,
                                    onReject = {
                                        viewModal.changeTicketStatus(tickets.ticket.ticketId, "1")
                                    },
                                    onConfirm = {
                                        viewModal.changeTicketStatus(tickets.ticket.ticketId, "0")
                                    },
                                    onDelivered = {
                                        viewModal.changeTicketStatus(tickets.ticket.ticketId, "3")
                                    },
                                    onTicketClicked = {
                                        NavigationProvider.navController.navigate(Routes.ProductDetailsPage(it))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun SellerTicketCard(
    item: ProductTicket,
    onReject: () -> Unit,
    onConfirm: () -> Unit,
    onDelivered: () -> Unit,
    onTicketClicked:(String) -> Unit
) {
    val productImage = item.imageUrl

    val statusMap = mapOf(
        "0" to ("Approved" to Color.Green),
        "1" to ("Rejected" to Color.Red),
        "2" to ("Waiting" to Color.Gray),
        "3" to ("Delivered" to Color.Blue)
    )
    val (statusText, textColor) = statusMap[item.ticket.ticketStatus] ?: ("Unknown" to Color.Black)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTicketClicked(item.ticket.productId) }
            .padding(5.dp),
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row {
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

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = item.productName.toNameFormat(), fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (item.ticket.sellingPrice != "-1")
                                item.ticket.sellingPrice.toTwoDecimalPlaces().toRupee()
                            else "On Call",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "QTY: ${item.ticket.quantity}",
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Size: ${item.ticket.sizeName}",
                            fontSize = 12.sp
                        )
                        Text(
                            text = item.ticket.colour,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Util.formatDateWithTimestamp(item.ticket.updatedAt),
                            fontSize = 12.sp
                        )

//                        Text(
//                            text = statusText,
//                            color = textColor,
//                            fontWeight = FontWeight.SemiBold
//                        )

                        // Status Text
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)  // Define a fixed size
                                    .clip(CircleShape)
                                    .background(textColor)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = statusText,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                color = textColor,
                                fontWeight = FontWeight.Normal,
                            )
                        }

                    }


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.userDetails?.userName ?: "Unknown", fontSize = 14.sp)
                        Text(
                            text = item.userDetails?.mobileNo ?: "N/A",
                            fontSize = 14.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            val showStatusLayout = item.ticket.ticketStatus == "2"
            val showDeliveredButton = item.ticket.ticketStatus == "0"

            if (showStatusLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier.weight(1f)
                            .padding(start = 3.dp, end = 3.dp, top = 3.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        modifier = Modifier.weight(1f)
                            .padding(start = 3.dp, end = 3.dp, top = 3.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Confirm")
                    }
                }
            }

            if (showDeliveredButton) {
                Button(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 3.dp, end = 3.dp, top = 3.dp),
                    onClick = onDelivered,
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("Delivered")
                }
            }
        }
    }
}
