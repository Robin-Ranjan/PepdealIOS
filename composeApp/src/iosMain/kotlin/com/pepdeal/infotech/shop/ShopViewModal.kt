package com.pepdeal.infotech.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.banner.BannerMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModal : ViewModel() {
    private val shopRepo = ShopRepo()
    private val _shops = MutableStateFlow<List<ShopWithProducts>>(emptyList())
    val shops: StateFlow<List<ShopWithProducts>> = _shops.asStateFlow()

    private val _bannerList = MutableStateFlow<List<BannerMaster>>(emptyList())
    val bannerList: StateFlow<List<BannerMaster>> = _bannerList.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var lastShopId: String? = null

    fun loadMoreShops() {
        if (_isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shopRepo.getActiveShopsFlowPaginationEmitWithFilter(lastShopId)
                    .collect { newShop ->  // ✅ newShop is a single item, not a list
                        if (newShop.shop.shopId != lastShopId) { // ✅ Prevent duplicate shop
                            lastShopId = newShop.shop.shopId
                            _shops.update { oldList ->
                                (oldList + newShop).distinctBy { it.shop.shopId }
                            }
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTheBannerList(){
        viewModelScope.launch {
           val bannerList =  shopRepo.getActiveBannerImages()
            _bannerList.value =  bannerList
        }
    }
}