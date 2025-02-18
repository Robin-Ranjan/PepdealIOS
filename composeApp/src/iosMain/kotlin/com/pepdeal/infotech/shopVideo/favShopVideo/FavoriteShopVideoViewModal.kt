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

class FavoriteShopVideoViewModal():ViewModel() {

    private val repo = FavoriteShopVideoRepo()
    private val shopVideosRepo = ShopVideosRepo()
    private val _shopVideos = MutableStateFlow<List<ShopVideoWithShopDetail>>(emptyList())
    val shopVideos: StateFlow<List<ShopVideoWithShopDetail>> = _shopVideos.asStateFlow()

     fun fetchShopVideos(userId:String) {
        viewModelScope.launch {
            repo.getFavoriteShopVideoForUserFlow(userId)
                .catch { e ->
                    println("Error: ${e.message}")
                }
                .collect { shopVideoWithShopDetail ->
                    _shopVideos.update { currentList ->
                        currentList + shopVideoWithShopDetail
                    }
                }
        }
    }

    fun removeFavVideo(userId: String,shopId:String){
        viewModelScope.launch {
            shopVideosRepo.removeFavoriteItem(userId, shopId) {
                _shopVideos.value = _shopVideos.value.filterNot { it.shopVideosMaster.shopId == shopId }
            }
        }
    }

    fun resetProduct(){
        _shopVideos.value = emptyList()
    }
}