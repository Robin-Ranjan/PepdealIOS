package com.pepdeal.infotech.categoriesProduct.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.categoriesProduct.repo.CategoryWiseProductRepository
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryWiseProductViewModal(
    private val categoryRepo: CategoryWiseProductRepository,
    private val favRepo: FavouriteProductRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subCategoryName =
        savedStateHandle.toRoute<Routes.CategoryWiseProductPage>().subCategoryName

    private val _state =
        MutableStateFlow(CategoryWiseProductState(subCategoryName = subCategoryName))
    val state = _state.asStateFlow()
        .onStart { observeUserLogin() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CategoryWiseProductState()
        )

    fun onAction(action: Action) {
        when (action) {
            is Action.OnToggleFavoriteStatus -> {
                if (_state.value.isLoggedIn) {
                    toggleFavoriteStatus(action.productId)
                } else {
                    _state.update { it.copy(snackBarMessage = SnackBarMessage.Error("Login Please")) }
                }
            }

            is Action.OnUserIdUpdate -> {
                _state.update { it.copy(userId = action.userId) }
            }

            Action.OnClearSnackBar -> _state.update { it.copy(snackBarMessage = null) }
        }
    }

    private fun observeUserLogin() {
        viewModelScope.launch {
            val user = preferencesRepository.getDataClass(
                key = PreferencesKeys.user_data_key,
                serializer = UserMaster.serializer(),
            )
            _state.update { it.copy(userId = user?.userId) }
            fetchCategoryProducts(subCategoryName, user?.userId)
        }
    }

    private fun fetchCategoryProducts(subCategoryName: String, userId: String? = null) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            categoryRepo.getTheCategoryWiseProduct(subCategoryName, userId = userId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            val product = result.data
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    data = it.data + product
                                )
                            }
                        }

                        is AppResult.Error -> {
                            if (result.error.type == DataError.RemoteType.EMPTY_RESULT) {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error("No Product found fo this category.")
                                    )
                                }
                            } else {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        snackBarMessage = SnackBarMessage.Error(result.error.message.toString())
                                    )
                                }
                            }

                        }
                    }
                }
        }
    }

    private fun toggleFavoriteStatus(productId: String) {
        val currentState = _state.value
        val productModel = currentState.data.find { it.product.productId == productId } ?: return

        viewModelScope.launch {
            if (!productModel.isFavorite) {
                favRepo.addFavorite(
                    product = FavoriteProductMaster(
                        favId = "",
                        productId = productModel.product.productId,
                        userId = currentState.userId ?: "-1",
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )
                ).onSuccess {
                    _state.update {
                        it.copy(data = it.data.map { p ->
                            if (p.product.productId == productId) p.copy(isFavorite = true) else p
                        })
                    }
                }.onError { e ->
                    _state.update {
                        it.copy(snackBarMessage = SnackBarMessage.Error(e.message.toString()))
                    }
                }
            } else {
                favRepo.removeFavoriteItem(
                    userId = currentState.userId ?: "-1",
                    productId = productId
                ).collect { result ->
                    result.onSuccess {
                        _state.update {
                            it.copy(data = it.data.map { p ->
                                if (p.product.productId == productId) p.copy(isFavorite = false) else p
                            })
                        }
                    }
                    result.onError { e ->
                        _state.update {
                            it.copy(snackBarMessage = SnackBarMessage.Error(e.message.toString()))
                        }
                    }
                }
            }
        }
    }

    data class CategoryWiseProductState(
        val data: List<CategoryWiseProductModel> = emptyList(),
        val subCategoryName: String = "",
        val userId: String? = null,
        val isLoading: Boolean = false,
        val snackBarMessage: SnackBarMessage? = null
    ) {
        val isLoggedIn: Boolean = userId != null
        val isEmpty: Boolean
            get() = data.isEmpty() && !isLoading
    }

    data class CategoryWiseProductModel(
        val product: ShopItems,
        val isFavorite: Boolean,
    )

    sealed interface Action {
        data object OnClearSnackBar : Action
        data class OnUserIdUpdate(val userId: String?) : Action
        data class OnToggleFavoriteStatus(val productId: String) : Action
    }
}