package com.pepdeal.infotech.placeAPI.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.checkPermission
import com.pepdeal.infotech.placeAPI.viewModel.LocationViewModel
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel
import pepdealios.composeapp.generated.resources.Res
import pepdealios.composeapp.generated.resources.manrope_bold

@Composable
fun SearchAddressRoot(viewModel: LocationViewModel = koinViewModel()) {

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackBar = remember { SnackbarHostState() }

    SearchAddressScreen(uiState = uiState, snackBar = snackBar, onAction = viewModel::onAction)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAddressScreen(
    uiState: LocationViewModel.UiState,
    snackBar: SnackbarHostState,
    onAction: (LocationViewModel.Action) -> Unit,
) {

    val factory = rememberPermissionsControllerFactory()
    var expanded by remember { mutableStateOf(false) }
    val controller = remember(factory) {
        factory.createPermissionsController()
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
                            onAction(LocationViewModel.Action.OnBackClick)
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
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { newValue ->
                            onAction(LocationViewModel.Action.UpdateSearchQuery(newValue))
                            expanded = newValue.isNotEmpty()
                        },
                        label = { Text("Search Places") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onAction(LocationViewModel.Action.UpdateSearchQuery(uiState.query))
                            }
                        )
                    )

                    LocationSelector(addressDetails = {
                        onAction(
                            LocationViewModel.Action.OnAddressChange(
                                it.address,
                                it.latitude,
                                it.longitude
                            )
                        )
                    }, onDismiss = {

                    })

                    AnimatedVisibility(visible = expanded) {
                        LazyColumn(
                            modifier = Modifier
                                .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                        ) {
                            items(uiState.predictions) { prediction ->
                                PredictionItem(prediction) {
                                    onAction(LocationViewModel.Action.OnPlaceSelected(prediction))
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}
