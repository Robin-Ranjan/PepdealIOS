package com.pepdeal.infotech.shop.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductUiDto
import com.pepdeal.infotech.product.productUseCases.ProductUseCase
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchProductViewmodel(
    private val productUseCase: ProductUseCase,
    private val favouriteProductRepository: FavouriteProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val query = savedStateHandle.toRoute<Routes.ProductSearchRoute>().query
    private val userId = savedStateHandle.toRoute<Routes.ProductSearchRoute>().userId

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
        .onStart {
            _state.update {
                it.copy(
                    userId = userId,
                    query = query
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )

    init {
        searchProducts(query, userId)
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.OnFavClicked -> {
                if (userId != null) {
                    toggleSearchedFavoriteStatus(action.product.shopItem.productId, userId)
                } else {
                    _state.update { it.copy(error = SnackBarMessage.Error("Please login to add to favorites")) }
                }
            }

            Action.OnResetMessage -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun searchProducts(query: String, userId: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            productUseCase.searchProduct(
                searchQuery = query,
                userId = userId
            )
                .catch { e ->
                    print(e)
                    _state.update { it.copy(isEmpty = true, isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    products = currentState.products + result.data
                                )
                            }
                        }

                        is AppResult.Error -> {

                        }
                    }
                }
        }
    }

    private fun toggleSearchedFavoriteStatus(productId: String, userId: String) {
        viewModelScope.launch {
            val currentList = _state.value.products
            val targetItem = currentList.find { it.shopItem.productId == productId }

            val isFav = targetItem?.isFavourite ?: false

            if (isFav) {
                // Safely collect and update
                favouriteProductRepository.removeFavoriteItem(userId, productId)
                    .collect { _ ->
                        val updatedList = currentList.map {
                            if (it.shopItem.productId == productId) {
                                it.copy(isFavourite = false)
                            } else it
                        }
                        _state.update { it.copy(products = updatedList) }
                    }
            } else {
                // No collection; just update directly
                favouriteProductRepository.addFavorite(
                    product = FavoriteProductMaster(
                        favId = "",
                        productId = productId,
                        userId = userId,
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )
                )
                val updatedList = currentList.map {
                    if (it.shopItem.productId == productId) {
                        it.copy(isFavourite = true)
                    } else it
                }
                _state.update { it.copy(products = updatedList) }
            }
        }
    }


    data class UiState(
        val isLoading: Boolean = false,
        val userId: String? = null,
        val query: String = "",
        val products: List<ProductUiDto> = emptyList(),
        val error: SnackBarMessage? = null,
        val isEmpty: Boolean = false
    )

    sealed interface Action {
        object OnResetMessage : Action
        data class OnFavClicked(val product: ProductUiDto) : Action
    }
}