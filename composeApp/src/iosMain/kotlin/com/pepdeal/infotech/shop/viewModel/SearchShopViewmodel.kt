package com.pepdeal.infotech.shop.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.shop.shopUseCases.ShopUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchShopViewmodel(
    private val shopUseCase: ShopUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val query = savedStateHandle.toRoute<Routes.ShopSearchRoute>().query
    private val userId = savedStateHandle.toRoute<Routes.ShopSearchRoute>().userId
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
        searchShops(query)
    }

    private fun searchShops(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            shopUseCase.searchShop(searchQuery = query)
                .catch { e ->
                    _state.update { it.copy(isEmpty = true, isLoading = false) }
                }
                .collect { result ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            shops = currentState.shops + result
                        )
                    }
                }
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val userId: String? = null,
        val query: String = "",
        val shops: List<ShopWithProducts> = emptyList(),
        val error: String? = null,
        val isEmpty: Boolean = false
    )
}