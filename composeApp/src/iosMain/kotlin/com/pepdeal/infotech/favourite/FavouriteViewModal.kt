package com.pepdeal.infotech.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.product.FavProductWithImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteProductViewModal : ViewModel() {
    private val favRepo = FavouritesRepo()
    private val _favoriteProduct =
        MutableStateFlow<List<FavProductWithImages>>(emptyList())
    val favoriteProduct: StateFlow<List<FavProductWithImages>> get() = _favoriteProduct

    private var currentFavoriteList: MutableList<FavProductWithImages> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> get() = _isEmpty

    fun getAllFavoriteProduct(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            favRepo.getFavoriteProductsForUserFlow(userId)
                .collect { product ->
                    product?.let {
                        // ✅ Filter out duplicates before adding
                        if (currentFavoriteList.none { it.product.productId == product.product.productId }) {
                            currentFavoriteList.add(product)
                            _favoriteProduct.value = currentFavoriteList.toList()
                        }
                        if (_isLoading.value) _isLoading.value = false
                    } ?: run {
                        _isLoading.value = false
                        _isEmpty.value = true
                    }
                }
        }
    }

    fun resetProduct() {
        _favoriteProduct.value = emptyList()
        currentFavoriteList = mutableListOf()
        _isEmpty.value = false
        _isLoading.value = false
    }

    fun removeFavItem(userId: String, productId: String) {
        viewModelScope.launch {
            favRepo.removeFavoriteItem(userId, productId) {
                _favoriteProduct.value =
                    _favoriteProduct.value.filterNot { it.product.productId == productId }

                if(_favoriteProduct.value.isEmpty()) _isEmpty.value = true
            }
        }
    }
}