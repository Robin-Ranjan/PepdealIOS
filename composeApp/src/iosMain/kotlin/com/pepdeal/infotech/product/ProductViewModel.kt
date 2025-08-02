package com.pepdeal.infotech.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.product.productUseCases.ProductUseCase
import com.pepdeal.infotech.product.repository.AlgoliaProductSearchTagRepository
import com.pepdeal.infotech.shop.viewModel.AddressData
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ProductViewModel(
    private val productUseCase: ProductUseCase,
    private val productAlgolia: AlgoliaProductSearchTagRepository,
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

    private var lastSuccessfulResult: SearchProductTagSummaryResult? = null
    private val _actions = MutableSharedFlow<ProductSearchSuggestion>()

    private val _searchTags = MutableStateFlow(SearchProductTags())
    val searchTags: StateFlow<SearchProductTags> = _searchTags.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _searchTags.value
        )
    private var lastSearchQuery: String = ""
    private var isFetching = false

    init {
        viewModelScope.launch {
            _actions
                .filterIsInstance<ProductSearchSuggestion.Search>()
                .map { it.query.trim() }
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun performSearch(query: String) {
        if (query == lastSearchQuery) return

        lastSearchQuery = query

        viewModelScope.launch {
            if (query.isBlank()) {
                lastSuccessfulResult?.let { result ->
                    _searchTags.update {
                        it.copy(
                            topSearchTags = result.topSearchTags,
                            topProductNames = result.topProductNames
                        )
                    }
                }
                return@launch
            }
            _searchTags.update { it.copy(isLoading = true, isEmpty = false) }

            try {
                var emitted = false
                productAlgolia.searchSummary(query).collectLatest { result ->
                    result.onSuccess {
                        if (it.topSearchTags.isNotEmpty() || it.topProductNames.isNotEmpty()) {
                            lastSuccessfulResult = it
                            _searchTags.update { tags ->
                                tags.copy(
                                    topSearchTags = it.topSearchTags,
                                    topProductNames = it.topProductNames,
                                    isLoading = false
                                )
                            }
                            emitted = true
                        }
                    }

                    result.onFailure { e ->
                        e.printStackTrace()
                        _searchTags.update { it.copy(isEmpty = true, isLoading = false) }
                        emitted = true
                    }
                }

                if (!emitted) {
                    _searchTags.update { it.copy(isEmpty = true, isLoading = false) }
                }

            } catch (e: CancellationException) {
                e.printStackTrace()
                println(e.message)
            } catch (e: Exception) {
                e.printStackTrace()
                _searchTags.update { it.copy(isEmpty = true, isLoading = false) }
            } finally {
                isFetching = false
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.OnResetMessage -> updateState(message = null)
            is Action.OnClickProduct -> {
                NavigationProvider.navController.navigate(
                    Routes.ProductDetailsPage(action.product.productId)
                )
            }

            is Action.OnFavClick -> {
                println("Product: ${action.product}")
                if (_state.value.user != null) {
                    println("${_state.value.user}")
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

            is Action.OnSearchQueryChange -> {
                viewModelScope.launch {
                    _actions.emit(ProductSearchSuggestion.Search(action.query))
                }
                _searchTags.update { it.copy(query = action.query) }
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
            _state.update { currentState ->
                val updatedProducts = currentState.products.map { product ->
                    if (product.shopItem.productId == productId) {
                        val newIsFav = !product.isFavourite

                        launch {
                            if (newIsFav) {
                                favouriteProductRepository.addFavorite(
                                    FavoriteProductMaster(
                                        favId = "",
                                        productId = productId,
                                        userId = userId,
                                        createdAt = Util.getCurrentTimeStamp(),
                                        updatedAt = Util.getCurrentTimeStamp()
                                    )
                                )
                            } else {
                                favouriteProductRepository.removeFavoriteItem(userId, productId)
                                    .catch { println("❌ removeFavorite failed: ${it.message}") }
                                    .collect { }
                            }
                        }

                        product.copy(isFavourite = newIsFav)
                    } else {
                        product
                    }
                }
                currentState.copy(products = updatedProducts)
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

    data class SearchProductTags(
        val query: String = "",
        val topSearchTags: List<String> = emptyList(),
        val topProductNames: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val isEmpty: Boolean = false
    )

    sealed interface Action {
        data object OnResetMessage : Action
        data object OnLocationClick : Action
        data class OnClickProduct(val product: ShopItems) :
            Action

        data class OnFavClick(val product: ShopItems) :
            Action


        data class OnLocationChange(val location: AddressData) : Action

        data class OnSearchQueryChange(val query: String) : Action
    }

    sealed class ProductSearchSuggestion {
        data class Search(val query: String) : ProductSearchSuggestion()
    }
}

data class SearchProductTagSummaryResult(
    val topSearchTags: List<String>,
    val topProductNames: List<String>
)