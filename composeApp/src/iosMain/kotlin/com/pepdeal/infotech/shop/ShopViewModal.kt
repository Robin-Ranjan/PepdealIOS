package com.pepdeal.infotech.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.ShopRepo
import com.pepdeal.infotech.ShopWithProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModal :ViewModel() {
    private val shopRepo = ShopRepo()
    private val _shops = MutableStateFlow<List<ShopWithProducts>>(emptyList())
    val shops: StateFlow<List<ShopWithProducts>> = _shops.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var lastShopId: String? = null

    fun loadMoreShops() {
        if (_isLoading.value) return // Prevent duplicate loading

        _isLoading.value = true
        viewModelScope.launch {
            try {
                shopRepo.getActiveShopsFlowPaginationEmitWithFilter(lastShopId).collect { newShops ->
                        lastShopId = newShops.shop.shopId // Update last shopId for pagination
                        _shops.update { oldList->
                            (oldList + newShops).distinctBy { it.shop.shopId }
                        }
                    _isLoading.value = false
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}