package com.pepdeal.infotech.shop.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.banner.BannerMaster
import com.pepdeal.infotech.banner.getActiveBannerImages
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.shop.repository.AlgoliaShopSearchTagRepository
import com.pepdeal.infotech.shop.shopUseCases.ShopUseCase
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.NavigationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(FlowPreview::class)
class ShopViewModel(
    private val shopUseCase: ShopUseCase,
    private val shopSearchTagsRepo: AlgoliaShopSearchTagRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
        .onStart {
            observeUserLogin()
            getTheBannerList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )

    private var lastSuccessfulResult: SearchShopTagSummaryResult? = null
    private val _actions = MutableSharedFlow<ShopSearchSuggestion>()

    private val _searchTags = MutableStateFlow(SearchShopTags())
    val searchTags: StateFlow<SearchShopTags> = _searchTags.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _searchTags.value
        )
    private var lastSearchQuery: String = ""

    private var searchJob: Job? = null
    private var isFetching = false

    init {
        viewModelScope.launch {
            _actions
                .filterIsInstance<ShopSearchSuggestion.Search>()
                .map { it.query.trim() }
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    println("🔍 Searching for: $query")
                    searchJob?.cancel()

                    searchJob = launch {
                        performSearch(query)
                    }
                }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.OnResetMessage -> handleMessage(null)
            Action.OnLocationClick -> NavigationProvider.navController.navigate(Routes.AddressSearchRoute)
            is Action.OnLocationChange -> {
                updateState(
                    address = action.location,
                    shops = emptyList(),
                    isLoading = true
                )
                loadMoreShops(action.location.lat ?: 0.0, action.location.lng ?: 0.0)
            }

            is Action.OnSearchQueryChange -> {
                viewModelScope.launch {
                    _searchTags.update { it.copy(query = action.query) }
                    _actions.emit(ShopSearchSuggestion.Search(action.query))
                }
            }
        }
    }

    fun loadMoreShops(userLat: Double = 28.7162092, userLng: Double = 77.1170743) {
        if (isFetching) {
            println("🚫 Already fetching, skipping.")
            return
        }

        isFetching = true
        updateState(isLoading = true)
        println("🚀 Fetching shops...")

        viewModelScope.launch(Dispatchers.IO) {
            val newShops = _state.value.shops.toMutableList()

            try {
                println("🔍 Starting shop fetch at lat=$userLat, lng=$userLng") // Debug start

                shopUseCase.getShop(userLat = userLat, userLng = userLng)
                    .collect { response ->
                        when (response) {
                            is AppResult.Success -> {
                                val data = response.data
                                println("✅ Received shops: ${data.shop.shopName}") // Debug log

                                data.let { shop ->
                                    // Optional: avoid duplicates
                                    if (newShops.none { it.shop.shopId == data.shop.shopId }) {
                                        newShops.add(shop)
                                    }
                                }
                            }

                            is AppResult.Error -> {
                                updateState(isEmpty = true)
                                println("❌ Error fetching shops: ${response.error.message}") // Debug error
                                handleMessage(response.error.message)
                            }
                        }
                    }

                updateState(shops = newShops)
            } catch (e: Exception) {
                println("🚨 Exception during shop fetch: ${e.message}")
                e.printStackTrace()
                updateState(message = SnackBarMessage.Error(e.message ?: "Something went wrong"))
            } finally {
                isFetching = false
                println("📦 Finished fetching shops, isFetching = $isFetching")
            }
        }
    }

    fun performSearch(query: String) {
        if (query == lastSearchQuery || isFetching) return

        lastSearchQuery = query
        isFetching = true

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                lastSuccessfulResult?.let { result ->
                    _searchTags.update {
                        it.copy(
                            topSearchTags = result.topShopNames,
                            topShopNames = result.topShopNames
                        )
                    }
                }
                isFetching = false
                return@launch
            }

            _searchTags.update { it.copy(isLoading = true) }

            try {
                var emitted = false
                shopSearchTagsRepo.searchSummary(query).collectLatest { result ->
                    result.onSuccess {
                        if (it.topSearchTags.isNotEmpty() && it.topShopNames.isNotEmpty()) {
                            lastSuccessfulResult = it
                            _searchTags.update { tags ->
                                tags.copy(
                                    topSearchTags = it.topSearchTags,
                                    topShopNames = it.topShopNames,
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

            } catch (e: Exception) {
                e.printStackTrace()
                _searchTags.update { it.copy(isEmpty = true, isLoading = false) }
            } finally {
                isFetching = false
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
        }
    }

    fun getTheBannerList() {
        viewModelScope.launch {
            val bannerList = getActiveBannerImages().toMutableList()
            bannerList.sortBy { it.bannerOrder.toInt() }
            updateState(bannerList = bannerList)
        }
    }

    private fun updateState(
        isLoading: Boolean = false,
        message: SnackBarMessage? = null,
        address: AddressData? = _state.value.address,
        bannerList: List<BannerMaster> = _state.value.bannerList,
        shops: List<ShopWithProducts> = _state.value.shops,
        user: UserMaster? = _state.value.user,
        isEmpty: Boolean = _state.value.isEmpty
    ) {
        _state.update {
            it.copy(
                isLoading = isLoading,
                message = message,
                bannerList = bannerList,
                address = address,
                shops = shops,
                user = user,
                isEmpty = isEmpty
            )
        }
    }

    private fun handleMessage(
        message: String?,
        fallbackMessage: String = "Something went wrong",
        isError: Boolean = true
    ) {
        updateState(
            message = if (isError) SnackBarMessage.Error(
                message ?: fallbackMessage
            ) else SnackBarMessage.Success(message ?: fallbackMessage),
        )
    }

    data class UiState(
        val isLoading: Boolean = true,
        val message: SnackBarMessage? = null,
        val bannerList: List<BannerMaster> = emptyList(),
        val shops: List<ShopWithProducts> = emptyList(),
        val user: UserMaster? = null,
        val address: AddressData? = null,
        val isEmpty: Boolean = false
    )

    data class SearchShopTags(
        val query: String = "",
        val topSearchTags: List<String> = emptyList(),
        val topShopNames: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val isEmpty: Boolean = false
    )

    sealed interface Action {

        data class OnLocationChange(val location: AddressData) : Action
        data object OnResetMessage : Action

        data object OnLocationClick : Action

        data class OnSearchQueryChange(val query: String) : Action
    }

    sealed class ShopSearchSuggestion {
        data class Search(val query: String) : ShopSearchSuggestion()
    }
}

@Serializable
data class AddressData(
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class SearchShopTagSummaryResult(
    val topSearchTags: List<String>,
    val topShopNames: List<String>
)