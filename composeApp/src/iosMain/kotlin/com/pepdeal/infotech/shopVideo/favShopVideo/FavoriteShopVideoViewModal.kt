package com.pepdeal.infotech.shopVideo.favShopVideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.shopVideo.ShopVideosRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteShopVideoViewModal :ViewModel() {

    private val repo = FavoriteShopVideoRepo()
    private val shopVideosRepo = ShopVideosRepo()
    private val _shopVideos = MutableStateFlow<List<ShopVideoWithShopDetail>>(emptyList())
    val shopVideos: StateFlow<List<ShopVideoWithShopDetail>> = _shopVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> get() = _isEmpty.asStateFlow()

     fun fetchShopVideos(userId:String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.getFavoriteShopVideoForUserFlow(userId)
                .catch { e ->
                    println("Error: ${e.message}")
                }
                .collect { shopVideoWithShopDetail ->

                    shopVideoWithShopDetail?.let {
                        _shopVideos.update { currentList ->
                            currentList + shopVideoWithShopDetail
                        }
                        if (_isLoading.value) _isLoading.value = false
                    } ?: run {
                        _isLoading.value = false
                        _isEmpty.value = true
                    }
                }
        }
    }

    fun removeFavVideo(userId: String,shopId:String){
        viewModelScope.launch {
            shopVideosRepo.removeFavoriteItem(userId, shopId) {
                _shopVideos.value = _shopVideos.value.filterNot { it.shopVideosMaster.shopId == shopId }
                if(_shopVideos.value.isEmpty()) _isEmpty.value = true
            }
        }
    }

    fun resetProduct(){
        _shopVideos.value = emptyList()
        _isEmpty.value = false
        _isLoading.value = false
    }
}