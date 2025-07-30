package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.productUseCases.ProductUseCase
import com.pepdeal.infotech.shop.viewModel.AddressData
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productUseCase: ProductUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val favouriteProductRepository: FavouriteProductRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState()
        )

    init {
        viewModelScope.launch {
            observeUserLogin()
        }
    }

    private val _searchedProducts =
        MutableStateFlow<List<ProductUiDto>>(emptyList())
    val searchedProducts: StateFlow<List<ProductUiDto>> get() = _searchedProducts

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> get() = _isSearchLoading

    private var lastSearchQuery = MutableStateFlow("")
    private var searchJob: Job? = null
    private var isFetching = false

    fun onAction(action: Action) {
        when (action) {
            Action.OnResetMessage -> updateState(message = null)
            is Action.OnClickProduct -> {
                NavigationProvider.navController.navigate(
                    Routes.ProductDetailsPage(action.product.productId)
                )
            }

            is Action.OnFavClick -> {
                if (_state.value.user != null) {
                    toggleFavoriteStatus(
                        productId = action.product.productId,
                        userId = _state.value.user?.userId ?: ""
                    )
                } else {
                    updateState(message = SnackBarMessage.Error("Please login to add to favorites"))
                }
            }

            is Action.OnLocationChange -> {
                updateState(address = action.location)
                action.location.address?.let {
                    fetchItems(
                        userLat = action.location.lat ?: 0.0,
                        userLng = action.location.lng ?: 0.0,
                        userId = _state.value.user?.userId
                    )
                }
            }

            is Action.OnSearchedFavClick -> {
                if (_state.value.user != null) {
                    toggleSearchedFavoriteStatus(
                        productId = action.product.productId,
                        userId = _state.value.user?.userId ?: ""
                    )
                } else {
                    updateState(message = SnackBarMessage.Error("Please login to add to favorites"))
                }
            }

            is Action.OnLocationClick -> {
                NavigationProvider.navController.navigate(Routes.AddressSearchRoute)
            }
        }
    }

    private fun observeUserLogin() {
        viewModelScope.launch {
            val user = preferencesRepository.getDataClass(
                key = PreferencesKeys.user_data_key,
                serializer = UserMaster.serializer(),
            )
            updateState(user = user)
            fetchItems(userId = user?.userId)
        }
    }

    private fun toggleFavoriteStatus(productId: String, userId: String) {
        viewModelScope.launch {
            val isFav = _state.value.products.find {
                it.shopItem
                    .productId == productId
            }?.isFavourite ?: false

            if (isFav) {
                favouriteProductRepository.removeFavoriteItem(userId, productId).collect {
                    updateState(products = _state.value.products.map {
                        if (it.shopItem.productId == productId) {
                            it.copy(isFavourite = false)
                        } else {
                            it
                        }
                    })
                }
            } else {
                favouriteProductRepository.addFavorite(
                    product = FavoriteProductMaster(
                        favId = "",
                        productId = productId,
                        userId = userId,
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )
                )
                updateState(products = _state.value.products.map {
                    if (it.shopItem.productId == productId) {
                        it.copy(isFavourite = true)
                    } else {
                        it
                    }
                })
            }
        }
    }


    private fun toggleSearchedFavoriteStatus(productId: String, userId: String) {
        viewModelScope.launch {
            val currentList = _searchedProducts.value
            val targetItem = currentList.find { it.shopItem.productId == productId }

            val isFav = targetItem?.isFavourite ?: false

            if (isFav) {
                // Safely collect and update
                favouriteProductRepository.removeFavoriteItem(userId, productId)
                    .collect { _ ->
                        val updatedList = currentList.map {
                            if (it.shopItem.productId == productId) {
                                it.copy(isFavourite = false)
                            } else it
                        }
                        _searchedProducts.value = updatedList
                    }
            } else {
                // No collection; just update directly
                favouriteProductRepository.addFavorite(
                    product = FavoriteProductMaster(
                        favId = "",
                        productId = productId,
                        userId = userId,
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )
                )
                val updatedList = currentList.map {
                    if (it.shopItem.productId == productId) {
                        it.copy(isFavourite = true)
                    } else it
                }
                _searchedProducts.value = updatedList
            }
        }
    }

    fun fetchItems(
        userId: String? = null,
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
    ) {
        if (isFetching) {
            println("⛔ Skipping fetch: Already loading.")
            return
        }

        println("🚀 Starting fetch for products near ($userLat, $userLng) with userId = $userId")
        isFetching = true
        updateState(isLoading = true)

        val collectedProducts = mutableListOf<ProductUiDto>()

        viewModelScope.launch {
            productUseCase.fetchedProducts(
                userId = userId,
                userLat = userLat,
                userLng = userLng
            )
                .onEach { product ->
                    product.onSuccess { data ->
                        println("✅ Product fetched: ${data.shopItem}")
                        collectedProducts.add(data)
                    }.onError {
                        println("❌ Product fetch error: ${it.type} - ${it.message}")
                    }
                }
                .catch { e ->
                    e.printStackTrace()
                    println("⚠️ Error during product flow: ${e.message}")
                    isFetching = false
                }
                .onCompletion {
                    isFetching = false
                    updateState(isLoading = false, products = collectedProducts)
                    println("🏁 Fetch completed. Total products collected: ${collectedProducts.size}")
                }
                .launchIn(this)
        }
    }


    fun fetchSearchedItemsPage(query: String) {
        if (query.isEmpty()) {
            _searchedProducts.value = emptyList()
            lastSearchQuery.value = ""
            return
        }

        if (query != lastSearchQuery.value.trim()) {

            println("query :- $query")
            println("lastSearchQuery :- ${lastSearchQuery.value}")
            _searchedProducts.value = emptyList()
            lastSearchQuery.value = query
        } else {
            return
        }

        _isSearchLoading.value = true
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            println("📌 Search fetchItemsPage() called with query: $query")
            val lastProductId = _searchedProducts.value.lastOrNull()?.shopItem?.productId

            try {
                productUseCase.searchProduct(
                    lastProductId,
                    startIndex = null,
                    pageSize = 500,
                    searchQuery = query
                )
                    .collect { response ->

                        when (response) {
                            is AppResult.Error -> {
                                _isSearchLoading.value = false
                                _searchedProducts.value = emptyList()
                                return@collect
                            }

                            is AppResult.Success -> {
                                val newProduct = response.data
                                newProduct.let {
                                    _searchedProducts.update { oldList ->
                                        (oldList + newProduct).distinctBy { it.shopItem.productId }
                                    }
                                }
                                _isSearchLoading.value = false
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                println("⚠️ Error fetching products: ${e.message}")
            }
        }
    }

    private fun updateState(
        isLoading: Boolean = false,
        message: SnackBarMessage? = null,
        user: UserMaster? = _state.value.user,
        isEmpty: Boolean = _state.value.isEmpty,
        products: List<ProductUiDto> = _state.value.products,
        address: AddressData? = _state.value.address,
    ) {
        _state.update {
            it.copy(
                isLoading = isLoading,
                message = message,
                user = user,
                isEmpty = isEmpty,
                products = products,
                address = address
            )
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val message: SnackBarMessage? = null,
        val user: UserMaster? = null,
        val isEmpty: Boolean = false,
        val products: List<ProductUiDto> = emptyList(),
        val address: AddressData? = null,
    )

    sealed interface Action {
        data object OnResetMessage : Action
        data object OnLocationClick : Action
        data class OnClickProduct(val product: ShopItems) :
            Action

        data class OnFavClick(val product: ShopItems) :
            Action

        data class OnSearchedFavClick(val product: ShopItems) : Action


        data class OnLocationChange(val location: AddressData) : Action
    }
}