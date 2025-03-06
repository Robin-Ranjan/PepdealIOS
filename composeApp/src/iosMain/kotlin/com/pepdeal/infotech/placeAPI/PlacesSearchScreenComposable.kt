package com.pepdeal.infotech.placeAPI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pepdeal.infotech.checkPermission
import com.pepdeal.infotech.util.APIKEY
import com.pepdeal.infotech.util.NavigationProvider.navController
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.jordond.compass.geocoder.MobileGeocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.mobile
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

    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val snackBar = remember { SnackbarHostState() }


    // Observe search results
    LaunchedEffect(searchAddressQueryFlow) {
        searchAddressFlow(searchAddressQueryFlow, APIKEY.PLACE_API_KEY).collect { results ->
            println("results:- $results")
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

    LaunchedEffect(Unit) {
        checkPermission(
            permission = Permission.LOCATION,
            controller = controller,
            snackBarHostState = snackBar
        )
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackBar) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Search Address",
                            fontSize = 20.ssp,
                            lineHeight = 20.ssp,
                            fontFamily = FontFamily(Font(Res.font.manrope_bold)),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
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
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            },
            containerColor = Color.White

        ) { paddingValue ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValue)) {
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

                    LocationSelector(addressDetails = {
                        addressDetails(it)
                    }, onDismiss = {
                        onDismiss()
                    })

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
                                            fetchPlaceDetails(
                                                prediction.placeId,
                                                APIKEY.PLACE_API_KEY,
                                                prediction.sessionToken
                                            )
                                        }.getOrNull() // Prevents crash on failure

                                        placeDetails?.let {
                                            addressDetails(
                                                it.copy(
                                                    name = prediction.name,
                                                    address = prediction.address
                                                )
                                            )
                                            isFetchingDetails = false

                                            println("Place Details:- $placeDetails")
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

@Composable
fun LocationSelector(
    addressDetails: (PlacePrediction) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val geoLocation = rememberSaveable { Geolocator.mobile() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable {
                scope.launch {
                    isLoading = true // Start loading indicator

                    when (val result = geoLocation.current()) {
                        is GeolocatorResult.Success -> {
                            println("LOCATION: ${result.data.coordinates}")

                            val geocoder = MobileGeocoder()
                            val place = geocoder.placeOrNull(result.data.coordinates)

                            if (place != null) {
                                val coordinates = place.coordinates
                                val area = place.subLocality.orEmpty()
                                val city = place.locality.orEmpty()
                                val state = place.administrativeArea.orEmpty()
                                val country = place.country.orEmpty()
                                val postalCode = place.postalCode.orEmpty()
                                val street = place.thoroughfare.orEmpty()
                                val houseNumber = place.subThoroughfare.orEmpty()

                                // Construct full address dynamically
                                val fullAddress = listOfNotNull(
                                    houseNumber.takeIf { it.isNotEmpty() },
                                    street.takeIf { it.isNotEmpty() },
                                    area.takeIf { it.isNotEmpty() },
                                    city.takeIf { it.isNotEmpty() },
                                    state.takeIf { it.isNotEmpty() },
                                    postalCode.takeIf { it.isNotEmpty() },
                                    country.takeIf { it.isNotEmpty() }
                                ).joinToString(", ")

                                println("Full Address: $fullAddress")

                                addressDetails(
                                    PlacePrediction(
                                        placeId = "",
                                        latitude = coordinates.latitude,
                                        longitude = coordinates.longitude,
                                        city = city,
                                        state = state,
                                        area = area,
                                        address = fullAddress,
                                        name = ""
                                    )
                                )

                                onDismiss()
                            } else {
                                println("Error: Place details not found.")
                            }
                        }

                        is GeolocatorResult.Error -> {
                            println("LOCATION ERROR: ${result.message}")
                        }
                    }

                    isLoading = false // Stop loading indicator after fetching
                }
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Location Icon",
                tint = Color.Blue,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        }

        Text(
            text = "Select Your Current Location",
            color = Color.Blue, // Equivalent to holo_blue_light
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
