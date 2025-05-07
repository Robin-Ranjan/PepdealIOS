package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isSearchLoading = MutableStateFlow(false) // Loading state
    val isSearchLoading: StateFlow<Boolean> get() = _isSearchLoading

    private var lastSearchQuery = MutableStateFlow("")

//    private var lastSearchQuery: String = ""
    private var searchJob: Job? = null  // Track the ongoing search job

    // Define this at the ViewModel level
    private var currentPage = 0  // Page counter
    private val pageSize = 50    // Number of items per page
    private var endReached = false  // Flag to prevent further loading if no more data

    fun fetchItemsPage() {
        println("📌 fetchItemsPage() called")

        // Prevent duplicate load
        if (_isLoading.value) {
            println("⛔ Skipping fetch: Already loading.")
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val newProducts = mutableListOf<ShopItems>()

                productRepo.getAllNearbyProductsFlow()
                    .collect { product ->
                        newProducts.add(product)
//                        if(_isLoading.value) _isLoading.value = false
                    }

                println("📦 Fetched ${newProducts.size} nearby products")

                // ✅ Merge with existing, removing duplicates
                _products.update { currentList ->
                    (currentList + newProducts).distinctBy { it.productId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("⚠️ Error fetching products: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSearchedItemsPage(query: String) {
        if (query.isEmpty()) {
            _searchedProducts.value = emptyList()
            lastSearchQuery.value = ""
            return
        }

        // If the query has changed, reset search results and pagination.
        if (query != lastSearchQuery.value.trim()) {

            println("query :- $query")
            println("lastSearchQuery :- ${lastSearchQuery.value}")
            _searchedProducts.value = emptyList()
            lastSearchQuery.value = query
        }else {
            return
        }

        _isSearchLoading.value = true
        // **Cancel any ongoing search before starting a new one**
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            println("📌 Search fetchItemsPage() called with query: $query")
            var itemCount = 0 // Track how many items are loaded
            val lastProductId = _searchedProducts.value.lastOrNull()?.productId // Pagination index

            try {
                productRepo.getAllProductsSearchFlowPagination(lastProductId, pageSize, query)
                    .collect { newProduct ->
                        newProduct?.let {
                            _searchedProducts.update { oldList ->
                                (oldList + newProduct).distinctBy { it.productId }
                            }
                        } ?: run {
                            _isSearchLoading.value = false
                            _searchedProducts.value = emptyList()
                        }

//                        itemCount++
                        _isSearchLoading.value = false
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