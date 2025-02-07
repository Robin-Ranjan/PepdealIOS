package com.pepdeal.infotech.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.ProductWithImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteProductViewModal:ViewModel() {
    private val favRepo = FavouritesRepo()
    private val _favoriteProduct =
        MutableStateFlow<List<ProductWithImages>>(emptyList())
    val favoriteProduct: StateFlow<List<ProductWithImages>> get() = _favoriteProduct

    private var currentFavoriteList: MutableList<ProductWithImages> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getAllFavoriteProduct(userId:String){
        viewModelScope.launch {
            favRepo.getFavoriteProductsForUserFlow(userId)
                .collect{ product ->
                    currentFavoriteList.add(product)
                    _favoriteProduct.value = currentFavoriteList.toList()
                    if(_isLoading.value) _isLoading.value = false
                }
        }
    }

    fun resetProduct(){
        _favoriteProduct.value = emptyList()
        currentFavoriteList = mutableListOf()
    }

    fun removeFavItem(productId:String){
        viewModelScope.launch {
            favRepo.removeFavoriteItem(productId){
                _favoriteProduct.value = _favoriteProduct.value.filterNot { it.product.productId == productId }
            }
        }
    }
}