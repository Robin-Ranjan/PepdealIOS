package com.pepdeal.infotech.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pepdeal.infotech.core.base_ui.CustomSnackBarHost
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.screen.component.ProductCardNew
import com.pepdeal.infotech.shop.BackGroundColor
import com.pepdeal.infotech.shop.screen.SearchCard
import com.pepdeal.infotech.shop.viewModel.SearchProductViewmodel
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util.toNameFormat
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductSearchScreenRoot(viewModel: SearchProductViewmodel = koinViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            if (it.message.isNotBlank()) {
                snackBarHostState.showSnackbar(it.message)
                viewModel.onAction(SearchProductViewmodel.Action.OnResetMessage)
            }
        }
    }

    ProductSearchScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        snackBarHostState = snackBarHostState
    )
}

@Composable
fun ProductSearchScreen(
    uiState: SearchProductViewmodel.UiState,
    onAction: (SearchProductViewmodel.Action) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val statusBarHeight by rememberUpdatedState(
        newValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    )

    Scaffold(
        containerColor = BackGroundColor,
        snackbarHost = {
            CustomSnackBarHost(
                hostState = snackBarHostState,
                currentMessage = uiState.error
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = paddingValues.calculateBottomPadding() + 15.dp
                )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(statusBarHeight)
                    .background(color = Color.White)
            )

            SearchCard(
                onBackClick = { NavigationProvider.navController.popBackStack() },
                query = uiState.query.toNameFormat()
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(5.dp)
            ) {

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    items(uiState.products) { product ->
                        ProductCardNew(
                            shopItems = product.shopItem,
                            isFavorite = product.isFavourite,
                            onFavoriteClick = {
                                onAction(SearchProductViewmodel.Action.OnFavClicked(product))
                            },
                            onProductClicked = {
                                NavigationProvider.navController.navigate(
                                    Routes.ProductDetailsPage(
                                        product.shopItem.productId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}