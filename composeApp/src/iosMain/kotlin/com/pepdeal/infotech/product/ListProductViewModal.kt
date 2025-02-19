package com.pepdeal.infotech.product

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.Uri
import com.pepdeal.infotech.categories.ProductCategories
import com.pepdeal.infotech.categories.SubCategory
import com.pepdeal.infotech.color.ColorItem
import com.pepdeal.infotech.shop.modal.ProductMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListProductViewModal() : ViewModel() {
    private val repo = ListProductRepo()

    private val _registerProductResponse = MutableStateFlow<Pair<Boolean, String>?>(null)
    val registerProductResponse: StateFlow<Pair<Boolean, String>?> = _registerProductResponse

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun registerProduct(
        shopId: String,
        productMaster: ProductMaster,
        uriList: MutableList<ImageBitmap>,
    ) {
        _isUploading.value = true
        println(uriList.size)
        viewModelScope.launch {
            repo.addProductInTheShop(shopId, productMaster, uriList) { status , message ->
                _registerProductResponse.value = Pair(status,message)
                _isUploading.value = false
            }
        }
    }
    fun reset(){
        _registerProductResponse.value = null
        _isUploading.value = false
    }

    private val _selectedProductCategories = MutableStateFlow<ProductCategories?>(null)
    val selectedProductCategories: StateFlow<ProductCategories?> = _selectedProductCategories

    private val _selectedProductSubCategories = MutableStateFlow<SubCategory?>(null)
    val selectedProductSubCategories: StateFlow<SubCategory?> = _selectedProductSubCategories

    private val _selectedProductColours = MutableStateFlow<List<ColorItem>?>(null)
    val selectedProductColours: StateFlow<List<ColorItem>?> = _selectedProductColours

    fun updateProductCategories(productCategories: ProductCategories) {
        _selectedProductCategories.value = productCategories
    }

    fun updateProductSubCategories(productSubCategories: SubCategory) {
        _selectedProductSubCategories.value = productSubCategories
    }

    fun resetTheProductDetails() {
        _selectedProductCategories.value = null
        _selectedProductSubCategories.value = null
        _selectedProductColours.value = emptyList()
    }

    fun resetTheSelectedSubCategories() {
        _selectedProductSubCategories.value =
            SubCategory(id = 0, name = "", categoryId = 0, imageUrl = "", isSelected = false)
    }

    fun updateProductColours(colours: List<ColorItem>) {
        _selectedProductColours.value = colours
    }
}