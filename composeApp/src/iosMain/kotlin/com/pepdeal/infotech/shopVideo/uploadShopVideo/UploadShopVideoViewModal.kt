package com.pepdeal.infotech.shopVideo.uploadShopVideo

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.shopVideo.ShopVideosMaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadShopVideoViewModal() : ViewModel() {
    private val repo = UploadShopVideoRepo()

    private val _shopVideos = MutableStateFlow<ShopVideosMaster?>(null)
    val shopVideos: StateFlow<ShopVideosMaster?> get() = _shopVideos.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading :StateFlow<Boolean> get() = _isUploading.asStateFlow()

    fun getTheShopVideo(shopId: String) {
        viewModelScope.launch {
            repo.getShopVideoWithThumbNail(shopId,
                onSuccess = {
                    _shopVideos.value = it
                },
                onFailure = {
                    _shopVideos.value = null
                })
        }
    }

    fun uploadVideo(shopId: String, byteArray: ByteArray,thumbNailImage: ImageBitmap?) {
        viewModelScope.launch {
            _isUploading.value = true
            repo.uploadVideoWithDelete(byteArray, thumbNailImage,shopId,
                onSuccess = {

                },
                onProgress = {

                },
                onFailure = {

                })

            _isUploading.value = false
        }
    }

    sealed class UploadStatus {
        object Valid : UploadStatus()
        data class Invalid(val message: String) : UploadStatus()
        data class Uploading(val progress: Double) : UploadStatus()
        data class Success(val downloadUrl: String) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }
}