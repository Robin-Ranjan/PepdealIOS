package com.pepdeal.infotech.categoriesProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryWiseProductViewModal():ViewModel() {
    private val repo = CategoryWiseProductRepo()
    private val favRepo = FavouritesRepo()

    private val _products = MutableStateFlow<List<ShopItems>>(emptyList())
    val products: StateFlow<List<ShopItems>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    fun fetchCategoryProducts(subCategoryName: String) {
        _isLoading.value = true
        _isEmpty.value = false

        viewModelScope.launch {
            repo.getTheCategoryWiseProduct(subCategoryName)
                .catch { e ->
                    _isLoading.value = false
                    _isEmpty.value = true
                    println("Error: ${e.message}")
                }
                .collect { productList ->
                    _isLoading.value = false
                    _isEmpty.value = productList.isEmpty()
                    _products.update { it + productList }
                }
        }
    }

    fun checkFavoriteExists(userId: String, productId: String, callback: (Boolean) -> Unit) {
        // Query Firebase or local storage to check if product is in favorites
        viewModelScope.launch {
            val exists =
                favRepo.isFavorite(userId, productId) // Implement this function in your repo
            callback(exists)
        }
    }

    fun toggleFavoriteStatus(userId: String, productId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                favRepo.addFavorite(
                    product = FavoriteProductMaster(
                        favId = "",
                        productId = productId,
                        userId = userId,
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )
                )
            } else {
                favRepo.removeFavoriteItem(userId, productId) {
                }
            }
        }
    }

    fun reset(){
        _products.value = emptyList()
        _isEmpty.value = false
        _isLoading.value = false
    }

//    sealed class ProductState {
//        object Loading : ProductState()
//        object Empty : ProductState()
//        data class Success(val products: List<ShopItems>) : ProductState()
//        data class Error(val message: String) : ProductState()
//    }

}