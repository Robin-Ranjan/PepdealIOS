package com.pepdeal.infotech.shopVideo.favShopVideo.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.shopVideo.favShopVideo.repository.FavoriteShopVideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteShopVideoViewModal(
    savedStateHandle: SavedStateHandle,
    private val repository: FavoriteShopVideoRepository
) : ViewModel() {

    val userId = savedStateHandle.toRoute<Routes.FavoriteShopVideosPage>().userId

    private val _state = MutableStateFlow(FavoriteShopVideoState(userId = userId))
    val state = _state.asStateFlow()
        .onStart { fetchShopVideos(userId) }
        .stateIn(
            viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = FavoriteShopVideoState()
        )

    fun onAction(action: Action) {
        when (action) {
            is Action.RemoveFavVideo -> {
                removeFavVideo(_state.value.userId, action.shopId)
            }

            Action.ClearSnackBarMessage -> _state.update { it.copy(snackBarMessage = null) }
        }
    }

    private fun fetchShopVideos(userId: String) {
        viewModelScope.launch {
            repository.getFavoriteShopVideoForUserFlow(userId)
                .collect { response ->
                    when (response) {
                        is AppResult.Success -> {
                            val data = response.data
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    shopVideos = if (data != null) it.shopVideos + data else it.shopVideos
                                )
                            }
                        }

                        is AppResult.Error -> {
                            if (response.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error(
                                            "No Favourites Video Found"
                                        )
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error(response.error.message.toString())
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun removeFavVideo(userId: String, shopId: String) {
        viewModelScope.launch {
            repository.removeFavoriteShopVideo(userId, shopId)
                .collect { response ->
                    response.onSuccess {
                        val updatedList =
                            _state.value.shopVideos.filterNot { it.shopVideosMaster.shopId == shopId }
                        _state.update { it.copy(shopVideos = updatedList) }
                    }
                    response.onError { e ->
                        _state.update {
                            it.copy(
                                snackBarMessage = SnackBarMessage.Error(
                                    e.message.toString()
                                )
                            )
                        }
                    }
                }
        }
    }


    data class FavoriteShopVideoState(
        val shopVideos: List<ShopVideoWithShopDetail> = emptyList(),
        val isLoading: Boolean = false,
        val userId: String = "",
        val snackBarMessage: SnackBarMessage? = null
    ) {
        val isEmpty: Boolean
            get() = shopVideos.isEmpty() && !isLoading
    }

    sealed interface Action {
        data class RemoveFavVideo(val shopId: String) : Action
        data object ClearSnackBarMessage : Action
    }
}