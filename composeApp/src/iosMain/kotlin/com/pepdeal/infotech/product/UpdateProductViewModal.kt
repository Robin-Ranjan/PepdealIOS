package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shop.modal.ShopMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateProductViewModal():ViewModel() {
    private val repo = UpdateProductRepo()
    private val _productDetails = MutableStateFlow(ProductMaster())
    val productDetails : StateFlow<ProductMaster> get() = _productDetails.asStateFlow()

    private val _productImages = MutableStateFlow<List<ProductImageMaster>>(emptyList())
    val productImages : StateFlow<List<ProductImageMaster>> get() = _productImages.asStateFlow()

    private val _productLoading = MutableStateFlow(false)
    val productLoading : StateFlow<Boolean> get() = _productLoading.asStateFlow()

    fun fetchProductDetails(productId:String){
        _productLoading.value = true
        viewModelScope.launch {
            val productDetails = repo.fetchProductDetails(productId)
            if(productDetails!=null){
                _productDetails.value = productDetails
                _productLoading.value = false
                println(productDetails)
            }else{
                println(null)
            }

        }
    }

    fun fetchProductImages(productId: String){
        viewModelScope.launch {
            val productImages = repo.getProductImages(productId)
            _productImages.value = productImages
            println(_productImages.value)
        }
    }
}