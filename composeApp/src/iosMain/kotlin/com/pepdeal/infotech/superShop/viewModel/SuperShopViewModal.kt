package com.pepdeal.infotech.superShop.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.favourite_product.modal.FavProductWithImages
import com.pepdeal.infotech.favourite_product.viewModel.ProductFavAction
import com.pepdeal.infotech.favourite_product.viewModel.ProductFavUiState
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.superShop.model.SuperShopsWithProduct
import com.pepdeal.infotech.superShop.repository.SuperShopRepository
import com.pepdeal.infotech.superShop.repository.SuperShopRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SuperShopViewModal(
    private val superShopRepo: SuperShopRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId = savedStateHandle.toRoute<Routes.FavouritesProductRoute>().userId

    private val _state = MutableStateFlow(SuperShopUiState(userId = userId))
    val state = _state.asStateFlow()
        .onStart {
            fetchSuperShop(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SuperShopUiState()
        )

    fun onAction(action: SuperShopAction) {
        when (action) {
            is SuperShopAction.OnRemoveSuperShop -> removeSuperShop(userId, action.shopId)
            SuperShopAction.OnClearSnackBar -> _state.update { it.copy(snackBarMessage = null) }
        }
    }

    private fun fetchSuperShop(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            superShopRepo.getSuperShopWithProduct(userId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val superShop = result.data
                            _state.update {
                                it.copy(
                                    superShop = it.superShop + superShop,
                                    isLoading = false,
                                )
                            }
                        }

                        is AppResult.Error -> {
                            if (result.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update {
                                    it.copy(
                                        snackBarMessage = SnackBarMessage.Error(
                                            message = "No Super Shop Available"
                                        ),
                                        isLoading = false
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        snackBarMessage = SnackBarMessage.Error(
                                            message = result.error.message
                                                ?: "Something went wrong"
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

    private fun removeSuperShop(userId: String, shopId: String) {
        viewModelScope.launch {
            superShopRepo.removeSuperShop(userId = userId, shopId = shopId)
                .onSuccess {
                    val updatedSuperShop =
                        _state.value.superShop.filter { it.shop.shopId == shopId }
                    _state.update { it.copy(superShop = updatedSuperShop) }
                }
                .onError {
                    _state.update { it.copy(snackBarMessage = SnackBarMessage.Error("Something went wrong")) }
                }
        }
    }
}

data class SuperShopUiState(
    val superShop: List<SuperShopsWithProduct> = emptyList(),
    val isLoading: Boolean = false,
    val snackBarMessage: SnackBarMessage? = null,
    val userId: String = ""
) {
    val isEmpty: Boolean = superShop.isEmpty() && !isLoading
}

interface SuperShopAction {
    data class OnRemoveSuperShop(val shopId: String) : SuperShopAction
    data object OnClearSnackBar : SuperShopAction
}