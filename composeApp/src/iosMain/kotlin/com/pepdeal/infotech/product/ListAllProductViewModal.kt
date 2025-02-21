package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListAllProductViewModal():ViewModel() {

    private val repo = ListAllProductRepo()

    private val _productWithImages = MutableStateFlow<List<ProductWithImages>>(emptyList())
    val productWithImages : StateFlow<List<ProductWithImages>> get() = _productWithImages

    private var currentProductList: MutableList<ProductWithImages> = mutableListOf()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun getAllProduct(shopId:String){
        _isLoading.value = true
        viewModelScope.launch {
            repo.fetchAllProductOfShop(shopId)
                .collect{ product ->
                    currentProductList.add(product)
                    println(product)
                    _productWithImages.value = currentProductList.toList()
                    if (_isLoading.value) _isLoading.value = false
                }
        }
    }

    fun reset(){
        _productWithImages.value = emptyList()
        _isLoading.value = false
        currentProductList = mutableListOf()
    }
}