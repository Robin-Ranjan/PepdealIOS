package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.categories.ProductCategories
import com.pepdeal.infotech.categories.SubCategory
import com.pepdeal.infotech.color.ColorItem
import com.pepdeal.infotech.favourite.FavouritesRepo
import com.pepdeal.infotech.favourite.modal.FavoriteProductMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModal() : ViewModel() {
    private val productRepo = ProductRepo()
    private val favRepo = FavouritesRepo()
    private val _products =
        MutableStateFlow<List<ShopItems>>(emptyList()) // StateFlow to hold product data
    val products: StateFlow<List<ShopItems>> get() = _products

    private val _isLoading = MutableStateFlow(false) // Loading state
    val isLoading: StateFlow<Boolean> get() = _isLoading


    private var currentPage = 0 // Page counter
    private val pageSize = 10   // Number of items per page
    private var endReached = false // Prevent fetching more if no more data

    fun fetchItemsPage() {
        if (_isLoading.value || endReached) return // Prevent duplicate loading

        _isLoading.value = true

        viewModelScope.launch {
            var itemCount = 0 // Track how many items are loaded

            try {
                productRepo.getAllProductsFlowPagination(
                    (currentPage * pageSize).toString(),
                    pageSize
                )
                    .catch { exception ->
                        println("Caught Exception: $exception")
                    }
                    .collect { newProduct ->
                        // Using a mutable list to efficiently add items without creating new lists
                        _products.update { oldList ->
                            // Use `distinctBy` to filter out duplicates
                            (oldList + newProduct).distinctBy { it.productId }
                        }

                        // Increment page count, handle loading state
                        itemCount++
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Exception: ${e.message}")
            }

            // If fewer than `pageSize` items were added, it means no more data left
            if (itemCount < pageSize) {
                endReached = true
            }

            currentPage++ // Move to the next page
            _isLoading.value = false
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