package com.pepdeal.infotech.product.producrDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.productUseCases.ProductUseCase
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.tickets.domain.TicketRepository
import com.pepdeal.infotech.tickets.model.TicketMaster
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailsViewModal(
    private val ticketRepository: TicketRepository,
    private val favRepo: FavouriteProductRepository,
    private val productUseCase: ProductUseCase,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = savedStateHandle.toRoute<Routes.ProductDetailsPage>().productId
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)

    init {
        observeUserLogin()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.OnChangeDialogState -> updateState(showDialog = action.showDialog)

            is Action.OnResetMessage -> updateState(message = null)

            is Action.OnClickOfFloatingButton -> {
                if (_state.value.userId == null) {
                    updateState(message = SnackBarMessage.Error("Please login to add ticket"))
                } else if (_state.value.product?.isTicketActive == true) {
                    updateState(message = SnackBarMessage.Error("Ticket already exists"))
                } else {
                    updateState(showDialog = true)
                }
            }

            is Action.OnFavClick -> {
                val currentProduct = _state.value.product
                if (_state.value.userId != null && currentProduct != null) {
                    toggleFavoriteStatus(
                        _state.value.userId!!,
                        currentProduct.product.product.productId,
                        currentProduct.isFavourite
                    )
                } else {
                    updateState(message = SnackBarMessage.Error("Please login to ad Favorites"))
                }
            }

            is Action.AddTicket -> {
                addTicket(_state.value.userMobileNo!!, action.ticketMaster)
            }
        }
    }

    private fun fetchProductDetails(productId: String, userId: String? = null) {
        viewModelScope.launch {
            productUseCase.fetchProductDetail(productId = productId, userId = userId)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            updateState(product = result.data)
                        }

                        is AppResult.Error -> {
                            updateState(isEmpty = true)
                        }
                    }
                }
        }
    }

    fun toggleFavoriteStatus(userId: String, productId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val currentProduct = _state.value.product

            if (currentProduct != null) {
                if (isFavorite) {
                    favRepo.addFavorite(
                        FavoriteProductMaster(
                            favId = "",
                            productId = productId,
                            userId = userId,
                            createdAt = Util.getCurrentTimeStamp(),
                            updatedAt = Util.getCurrentTimeStamp()
                        )
                    ).onSuccess {
                        updateState(
                            product = currentProduct.copy(
                                isFavourite = true
                            ),
                            message = SnackBarMessage.Success("Added to favorites")
                        )
                    }
                } else {
                    favRepo.removeFavoriteItem(userId, productId).first()
                    updateState(
                        product = currentProduct.copy(
                            isFavourite = false
                        )
                    )
                }
            }
        }
    }

    private fun observeUserLogin() {
        viewModelScope.launch {
            val user = preferencesRepository.getDataClass(
                key = PreferencesKeys.user_data_key,
                serializer = UserMaster.serializer(),
            )
            updateState(userId = user?.userId, userMobileNo = user?.mobileNo)
            fetchProductDetails(productId, user?.userId)
        }
    }

    private fun updateState(
        isLoading: Boolean = false,
        product: ProductDetailUiModel? = _state.value.product,
        userId: String? = _state.value.userId,
        userMobileNo: String? = _state.value.userMobileNo,
        isEmpty: Boolean = false,
        isError: Boolean = false,
        message: SnackBarMessage? = null,
        showDialog: Boolean = _state.value.showDialog
    ) {
        _state.value = UiState(
            product = product,
            isLoading = isLoading,
            userId = userId,
            userMobileNo = userMobileNo,
            isEmpty = isEmpty,
            isError = isError,
            message = message,
            showDialog = showDialog
        )
    }

    fun addTicket(
        userMobileNo: String,
        ticketMaster: TicketMaster,
    ) {
        viewModelScope.launch {
            ticketRepository.addTicket(userMobileNo, ticketMaster)
                .onSuccess {
                    updateState(
                        message = SnackBarMessage.Success("Ticket Added Successfully"),
                        showDialog = false,
                        product = _state.value.product?.copy(isTicketActive = true)
                    )
                }
                .onError {
                    updateState(
                        message = SnackBarMessage.Error(it.message ?: "Failed to add ticket"),
                        showDialog = false
                    )
                }
        }
    }

    data class UiState(
        val product: ProductDetailUiModel? = null,
        val isLoading: Boolean = false,
        val userId: String? = null,
        val userMobileNo: String? = null,
        val isEmpty: Boolean = false,
        val isError: Boolean = false,
        val message: SnackBarMessage? = null,

        val showDialog: Boolean = false,
    )

    sealed interface Action {
        data class AddTicket(val ticketMaster: TicketMaster) : Action
        data class OnChangeDialogState(val showDialog: Boolean) : Action

        data object OnFavClick : Action
        data object OnResetMessage : Action

        data object OnClickOfFloatingButton : Action
    }
}

data class ProductDetailUiModel(
    val product: ProductWithImages,
    val shop: ShopMaster,
    val isFavourite: Boolean,
    val isTicketActive: Boolean,
)