package com.pepdeal.infotech.shopVideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shopVideo.favShopVideo.model.FavouriteShopVideo
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopVideosViewModal() : ViewModel() {

    private val repo = ShopVideosRepo()
    private val _shopVideos = MutableStateFlow<List<ShopVideoWithShopDetail>>(emptyList())
    val shopVideos: StateFlow<List<ShopVideoWithShopDetail>> = _shopVideos.asStateFlow()

    init {
        fetchShopVideos()
    }

    private fun fetchShopVideos() {
        viewModelScope.launch {
            repo.fetchShopVideoWithShopDetailsFlow()
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

    fun checkSaveShopExists(userId: String, shopId: String, callback: (Boolean) -> Unit) {
        // Query Firebase or local storage to check if product is in favorites
        viewModelScope.launch {
            val exists =
                repo.isSavedVideo(userId, shopId) // Implement this function in your repo
            callback(exists)
        }
    }

    fun toggleSaveShopStatus(userId: String, shopId: String,shopVideoId:String,isSavedShop: Boolean) {
        viewModelScope.launch {
            if (isSavedShop) {
                repo.addSaveVideo(
                    shopVideo = FavouriteShopVideo(
                        shop_id = shopId,
                        user_id = userId,
                        shop_video_id = shopVideoId,
                        createdAt = Util.getCurrentTimeStamp(),
                    )
                )
            } else {
                repo.removeFavoriteItem(userId, shopId) {
                }
            }
        }
    }
}