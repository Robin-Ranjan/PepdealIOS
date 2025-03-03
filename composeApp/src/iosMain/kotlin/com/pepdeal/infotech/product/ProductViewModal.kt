package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModal() : ViewModel() {
    private val productRepo = ProductRepo()
    private val favRepo = FavouritesRepo()
    private val _products =
        MutableStateFlow<List<ShopItems>>(emptyList()) // StateFlow to hold product data
    val products: StateFlow<List<ShopItems>> get() = _products

    private val _searchedProducts =
        MutableStateFlow<List<ShopItems>>(emptyList()) // StateFlow to hold product data
    val searchedProducts: StateFlow<List<ShopItems>> get() = _searchedProducts

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading
    private var lastSearchQuery: String = ""

    // Define this at the ViewModel level
    private var currentPage = 0  // Page counter
    private val pageSize = 50    // Number of items per page
    private var endReached = false  // Flag to prevent further loading if no more data

    fun fetchItemsPage() {
        println("📌 fetchItemsPage() called")  // ✅ Add this

        if (_isLoading.value || endReached) {
            println("⛔ Skipping fetch: isLoading=${_isLoading.value}, endReached=$endReached")
            return
        }
        _isLoading.value = true

        viewModelScope.launch {
            var itemCount = 0 // Track how many items are loaded
            val lastProductId = _products.value.lastOrNull()?.productId // ✅ Update pagination index
            try {
                productRepo.getAllProductsFlowPagination(lastProductId, pageSize)
                    .collect { newProduct ->
                        println("📌 New product received: ${newProduct.productName}")
                        _products.update { oldList ->
                            (oldList + newProduct).distinctBy { it.productId }
                        }
                        itemCount++
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                println("⚠️ Error fetching products: ${e.message}")
            }

            if (itemCount == 0) {
                endReached = true // ✅ Only stop if **no new items** were added
                println("🚨 No more products. Stopping pagination!")
            }

            currentPage++
            _isLoading.value = false
        }
    }

    fun fetchSearchedItemsPage(query: String) {
        println("📌 Search fetchItemsPage() called")  // ✅ Add this

        if (query.isEmpty()) {
            _searchedProducts.value = emptyList()
//            lastSearchedShopId = null
            lastSearchQuery = ""
            return
        }

        // If the query has changed from the previous one, clear the old results and reset pagination.
        if (query != lastSearchQuery) {
            _searchedProducts.value = emptyList()
//            lastSearchedShopId = null
            lastSearchQuery = query
        }

        // If already loading, simply return.
        if (_isLoading.value) return

        _isLoading.value = true

        viewModelScope.launch {
            var itemCount = 0 // Track how many items are loaded
            val lastProductId =
                _searchedProducts.value.lastOrNull()?.productId // ✅ Update pagination index
            try {
                productRepo.getAllProductsSearchFlowPagination(lastProductId, pageSize, query)
                    .collect { newProduct ->
                        println("📌 New product received: ${newProduct.productName}")
                        _searchedProducts.update { oldList ->
                            (oldList + newProduct).distinctBy { it.productId }
                        }
                        itemCount++
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                println("⚠️ Error fetching products: ${e.message}")
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
}