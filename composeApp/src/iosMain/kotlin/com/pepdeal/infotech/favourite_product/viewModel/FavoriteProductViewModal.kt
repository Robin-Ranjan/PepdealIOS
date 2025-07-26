package com.pepdeal.infotech.favourite_product.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.favourite_product.modal.FavProductWithImages
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteProductViewModal(
    private val productFavRepo: FavouriteProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId = savedStateHandle.toRoute<Routes.FavouritesProductRoute>().userId

    private val _state = MutableStateFlow(ProductFavUiState())
    val state = _state.asStateFlow()
        .onStart {
            getAllFavoriteProduct(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductFavUiState()
        )

    fun onAction(action: ProductFavAction) {
        when (action) {
            is ProductFavAction.OnRemoveFav -> removeFavItem(userId, action.productId)
            ProductFavAction.OnClearSnackBar -> _state.update { it.copy(snackBarMessage = null) }
        }
    }

    private fun getAllFavoriteProduct(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            productFavRepo.fetchFavoriteProducts(userId = userId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val ticket = result.data
                            _state.update { currentState ->
                                currentState.copy(
                                    favoriteProduct = (currentState.favoriteProduct + ticket).distinctBy { it.product.productId },
                                    isLoading = false
                                )
                            }

                        }

                        is AppResult.Error -> {
                            if (result.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update {
                                    it.copy(isLoading = false)
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        snackBarMessage = SnackBarMessage.Error(
                                            message = result.error.message ?: "Something went wrong"
                                        ),
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun removeFavItem(userId: String, productId: String) {
        viewModelScope.launch {
            productFavRepo.removeFavoriteItem(userId, productId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val updatedTickets =
                                _state.value.favoriteProduct.filterNot { it.product.productId == productId }
                            _state.update { it.copy(favoriteProduct = updatedTickets) }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(snackBarMessage = SnackBarMessage.Error("Something went wrong")) }
                        }
                    }

                }
        }
    }
}

data class ProductFavUiState(
    val favoriteProduct: List<FavProductWithImages> = emptyList(),
    val isLoading: Boolean = false,
    val snackBarMessage: SnackBarMessage? = null
) {
    val isEmpty: Boolean = favoriteProduct.isEmpty() && !isLoading
}

interface ProductFavAction {
    data class OnRemoveFav(val productId: String) : ProductFavAction
    data object OnClearSnackBar : ProductFavAction
}