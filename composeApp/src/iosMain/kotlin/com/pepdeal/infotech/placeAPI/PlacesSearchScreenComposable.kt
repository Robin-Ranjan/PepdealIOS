package com.pepdeal.infotech.placeAPI
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.util.APIKEY
import com.pepdeal.infotech.util.NavigationProvider.navController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesSearchScreen(addressDetails: (PlacePrediction) -> Unit, onDismiss: () -> Unit) {
    val searchQuery = remember { mutableStateOf("") }
    val searchAddressQueryFlow = remember { MutableStateFlow("") }
    var expanded by remember { mutableStateOf(false) }
    val predictions = remember { mutableStateListOf<PlacePrediction>() }
    var isFetchingDetails by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Observe search results
    LaunchedEffect(searchAddressQueryFlow) {
        searchAddressFlow(searchAddressQueryFlow, APIKEY.PLACE_API_KEY).collect { results ->
            predictions.clear()
            predictions.addAll(results)
        }
    }

    // Update search query flow when text input changes
    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.length > 2) {
            searchAddressQueryFlow.value = searchQuery.value
        }
    }

    MaterialTheme{
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Shop Details",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
//                            viewModel.reset()
//                            navController.popBackStack()
                            onDismiss()
                        }) {
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
                    )
                )
            },
            containerColor = Color.White

        ) { paddingValue->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValue)){
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { newValue ->
                            searchQuery.value = newValue
                            expanded = newValue.isNotEmpty()
                        },
                        label = { Text("Search Places") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = expanded) {
                        LazyColumn(
                            modifier = Modifier
                                .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                        ) {
                            items(predictions) { prediction ->
                                PredictionItem(prediction) {
                                    isFetchingDetails = true
                                    scope.launch {
                                        val placeDetails = runCatching {
                                            fetchPlaceDetails(prediction.placeId, APIKEY.PLACE_API_KEY, prediction.sessionToken)
                                        }.getOrNull() // Prevents crash on failure

                                        placeDetails?.let {
                                            addressDetails(it.copy(name = prediction.name, address = prediction.address))
                                            isFetchingDetails = false
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isFetchingDetails) {
               CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun PredictionItem(
    prediction: PlacePrediction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = 4.dp,
        shape = androidx.compose.material.MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = prediction.name,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prediction.address,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}