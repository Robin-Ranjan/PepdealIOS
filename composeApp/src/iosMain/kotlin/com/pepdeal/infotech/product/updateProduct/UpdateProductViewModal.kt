package com.pepdeal.infotech.product.updateProduct

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateProductViewModal : ViewModel() {
    private val repo = UpdateProductRepo()
    private val _productDetails = MutableStateFlow(ProductMaster())
    val productDetails: StateFlow<ProductMaster> get() = _productDetails.asStateFlow()

    private val _productImages = MutableStateFlow<List<ProductImageMaster>>(emptyList())
    val productImages: StateFlow<List<ProductImageMaster>> get() = _productImages.asStateFlow()

    private val _updateProductResponse = MutableStateFlow<Pair<Boolean, String>?>(null)
    val updateProductResponse: StateFlow<Pair<Boolean, String>?> = _updateProductResponse

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _productLoading = MutableStateFlow(false)
    val productLoading: StateFlow<Boolean> get() = _productLoading.asStateFlow()

    fun fetchProductDetails(productId: String) {
        _productLoading.value = true
        viewModelScope.launch {
            val productDetails = repo.fetchProductDetails(productId)
            if (productDetails != null) {
                _productDetails.value = productDetails.product
                _productImages.value = productDetails.images
                _productLoading.value = false
                println(productDetails)
            } else {
                println(null)
            }
        }
    }

    fun updateProductDetails(
        productId: String,
        updatedProductMaster: ProductMaster,
        isImageEdited: Boolean,
        newUriList: MutableList<ImageBitmap>,
    ) {
        _isUploading.value = true
        viewModelScope.launch {
            repo.updateProductWithImages(
                productId = productId,
                updatedProductMaster = updatedProductMaster,
                isImageEdited = isImageEdited,
                newUriList = newUriList
            ) { status, message ->
                _updateProductResponse.value = Pair(status, message)
                _isUploading.value = false
            }
        }
    }

    fun reset() {
        _productDetails.value = ProductMaster()
        _productLoading.value = false
        _productImages.value = emptyList()
        _updateProductResponse.value = null
    }
}