package com.pepdeal.infotech.shop.shopDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.superShop.SuperShopMaster
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopDetailsViewModal():ViewModel() {
    private val repo = ShopDetailsRepo()
    private val favRepo = FavouritesRepo()
    private val _shopDetails = MutableStateFlow(ShopMaster())
    val shopDetails : StateFlow<ShopMaster> get() = _shopDetails

    private val _shopProduct = MutableStateFlow<List<ProductWithImages>>(emptyList())
    val shopProduct : StateFlow<List<ProductWithImages>> get() = _shopProduct

    private val _shopLoading = MutableStateFlow(false)
    val shopLoading : StateFlow<Boolean> get() = _shopLoading

    private val _isSuperShop = MutableStateFlow(false)
    val isSuperShop : StateFlow<Boolean> get() = _isSuperShop

    private val _shopServices = MutableStateFlow<ShopStatusMaster?>(null)
    val shopServices: StateFlow<ShopStatusMaster?> get() = _shopServices

    fun fetchShopDetails(shopId:String){
        _shopLoading.value = true
        viewModelScope.launch {
            val shopDetails = repo.fetchShopDetails(shopId = shopId)
            if(shopDetails!=null){
                _shopDetails.value = shopDetails
                _shopLoading.value = false
            }
        }
    }

    fun fetchShopProducts(shopId:String){
        viewModelScope.launch {
            repo.getActiveProductsWithImages(shopId)
                .collect{ newProduct->
                    // Using a mutable list to efficiently add items without creating new lists
                    _shopProduct.update { oldList ->
                        // Use `distinctBy` to filter out duplicates
                        (oldList + newProduct).distinctBy { it.product.productId }
                    }
                }
        }

    }

    fun fetchShopServices(shopId: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val shopServiceStatus = repo.fetchShopServices(shopId)
                _shopServices.value = shopServiceStatus
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    fun reset(){
        _shopDetails.value = ShopMaster()
        _shopProduct.value = emptyList()
        _shopLoading.value = false
    }

    fun checkFavoriteExists(userId: String,productId: String, callback: (Boolean) -> Unit) {
        // Query Firebase or local storage to check if product is in favorites
        viewModelScope.launch {
            val exists = favRepo.isFavorite(userId,productId) // Implement this function in your repo
            callback(exists)
        }
    }

    fun toggleFavoriteStatus(userId:String,productId: String,isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                favRepo.addFavorite(product = FavoriteProductMaster(
                    favId = "",
                    productId = productId,
                    userId = userId,
                    createdAt = Util.getCurrentTimeStamp(),
                    updatedAt = Util.getCurrentTimeStamp()
                )
                )
            } else {
                favRepo.removeFavoriteItem(userId,productId) {
                }
            }
        }
    }

    fun checkSuperShopExist(userId:String,shopId:String){
        viewModelScope.launch {
            repo.checkSuperShopExists(shopId,userId){
                _isSuperShop.value = it
            }
        }
    }

    fun toggleSuperShop(userId: String,shopId: String,isSuperShop:Boolean){
        viewModelScope.launch {
            if(isSuperShop){
                repo.removeSuperShop(userId,shopId){
                    _isSuperShop.value = false
                }
            }else{
                repo.addSuperShop(superShop = SuperShopMaster(
                    superId = "",
                    userId = userId,
                    shopId=shopId,
                    createdAt = Util.getCurrentTimeStamp(),
                    updatedAt = Util.getCurrentTimeStamp()
                )
                )
                _isSuperShop.value = true
            }
        }
    }
}