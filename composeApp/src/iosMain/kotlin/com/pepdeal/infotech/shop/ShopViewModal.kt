package com.pepdeal.infotech.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.banner.BannerMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModal : ViewModel() {
    private val shopRepo = ShopRepo()
    private val _shops = MutableStateFlow<List<ShopWithProducts>>(emptyList())
    val shops: StateFlow<List<ShopWithProducts>> = _shops.asStateFlow()

    private val _searchedShops = MutableStateFlow<List<ShopWithProducts>>(emptyList())
    val searchedShops: StateFlow<List<ShopWithProducts>> = _searchedShops.asStateFlow()

    private val _bannerList = MutableStateFlow<List<BannerMaster>>(emptyList())
    val bannerList: StateFlow<List<BannerMaster>> = _bannerList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> get() = _isSearchLoading

    private var lastShopId: String? = null
    private var lastSearchedShopId: String? = null
    private var lastSearchQuery: String = ""

    private var searchJob: Job? = null  // Track the ongoing search job

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

    fun loadMoreSearchedShops(query: String) {
        // If the query is empty, we emit nothing.
        if (query.isEmpty()) {
            _searchedShops.value = emptyList()
            lastSearchedShopId = null
            lastSearchQuery = ""
            return
        }

        // If the query has changed from the previous one, clear the old results and reset pagination.
        if (query != lastSearchQuery) {
            _searchedShops.value = emptyList()
            lastSearchedShopId = null
            lastSearchQuery = query
        } else {
            return
        }

        _isSearchLoading.value = true

        searchJob?.cancel()

        println("loadMore Searched  called")
        try {
            searchJob = viewModelScope.launch {
                // Call your repository function that returns a Flow<ShopWithProducts>
                shopRepo.getActiveSearchedShopsFlowPagination(
                    lastSearchedShopId,
                    searchQuery = query
                )
                    .collect { newShop ->
                        // Prevent duplicates by checking lastSearchedShopId
                        if (newShop.shop.shopId != lastSearchedShopId) {
                            lastSearchedShopId = newShop.shop.shopId
                        }
                        _searchedShops.update { oldList ->
                            (oldList + newShop).distinctBy { it.shop.shopId }
                        }

                        if (_isSearchLoading.value) _isSearchLoading.value = false
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSearchLoading.value = false
        }
    }

    fun getTheBannerList() {
        viewModelScope.launch {
            val bannerList = shopRepo.getActiveBannerImages().toMutableList()
            bannerList.sortBy { it.bannerOrder.toInt() }
            _bannerList.value = bannerList
        }
    }
}