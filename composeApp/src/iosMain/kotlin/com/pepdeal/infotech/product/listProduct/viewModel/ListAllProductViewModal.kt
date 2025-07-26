package com.pepdeal.infotech.product.listProduct.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ListAllProductRepository
import com.pepdeal.infotech.product.ProductWithImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListAllProductViewModal(
    private val listAllProductRepo: ListAllProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val shopId = savedStateHandle.toRoute<Routes.ListAllProductPage>().shopId

    private val _state = MutableStateFlow(ListAllProductState())
    val state = _state.asStateFlow()
        .onStart {
            getAllProduct(shopId)
        }
        .stateIn(
            viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = ListAllProductState()
        )

    fun onAction(action: ListAllProductAction) {
        when (action) {
            ListAllProductAction.OnClearSnackBar -> {
                _state.update { it.copy(snackBarMessage = null) }
            }

            is ListAllProductAction.UpdateProductStatusByShopOwner -> {
                updateProductStatusByShopOwner(action.productId, action.status)
            }
        }
    }

    private fun getAllProduct(shopId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            listAllProductRepo.fetchAllProductOfShop(shopId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update {
                                it.copy(
                                    productWithImages = result.data,
                                    isLoading = false
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _state.update {
                                it.copy(
                                    snackBarMessage = SnackBarMessage.Success(
                                        result.error.message ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun updateProductStatusByShopOwner(productId: String, status: String) {
        viewModelScope.launch {
            listAllProductRepo.updateProductStatusByShopOwner(productId, status)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { currentState ->
                                val updatedList = currentState.productWithImages.map {
                                    if (it.product.productId == productId) {
                                        it.copy(product = it.product.copy(productActive = status))
                                    } else {
                                        it
                                    }
                                }
                                currentState.copy(productWithImages = updatedList)
                            }
                        }

                        is AppResult.Error -> {
                            _state.update {
                                it.copy(
                                    snackBarMessage = SnackBarMessage.Success(
                                        result.error.message ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
        }
    }

    data class ListAllProductState(
        val productWithImages: List<ProductWithImages> = emptyList(),
        val isLoading: Boolean = false,
        val snackBarMessage: SnackBarMessage? = null
    ) {
        val isEmpty: Boolean = productWithImages.isEmpty()
    }

    interface ListAllProductAction {
        data class UpdateProductStatusByShopOwner(val productId: String, val status: String) :
            ListAllProductAction
        data object OnClearSnackBar : ListAllProductAction
    }
}