package com.pepdeal.infotech.product.producrDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModal():ViewModel() {

    val repo = ProductDetailsRepo()
    private val favRepo = FavouritesRepo()

    private val _product = MutableStateFlow<ProductWithImages?>(null)
    val product:StateFlow<ProductWithImages?> get() = _product.asStateFlow()

    private val _shop = MutableStateFlow<ShopMaster?>(null)
    val shop:StateFlow<ShopMaster?> get() = _shop.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading : StateFlow<Boolean> get() = _isLoading

    fun getTheProductDetails(productId:String){
        _isLoading.value = true
        viewModelScope.launch {
            val productDetails = repo.fetchTheProductDetails(productId)
            _product.value = productDetails
            println(productDetails)
            _isLoading.value = false
        }
    }

    fun getTheShopDetails(shopId:String){
        viewModelScope.launch {
            val shopMaster = repo.fetchTheProductShopDetails(shopId)
            _shop.value = shopMaster
            println(shopMaster)
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
        _product.value = null
        _isLoading.value = false
    }
}