package com.pepdeal.infotech.placeAPI.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.placeAPI.PlacePrediction
import com.pepdeal.infotech.placeAPI.repository.AddressRepository
import com.pepdeal.infotech.shop.viewModel.AddressData
import com.pepdeal.infotech.util.APIKEY
import com.pepdeal.infotech.util.NavigationProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class LocationViewModel(private val addressRepository: AddressRepository) : ViewModel() {
    private var searchJob: Job? = null

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        println("LocationViewModel: Initializing...")
        setupSearchFlow()
    }

    private fun setupSearchFlow() {
        println("LocationViewModel: Setting up search flow...")
        viewModelScope.launch {
            state
                .map { it.query.trim() }
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    println("LocationViewModel: Search triggered for query = '$query'")

                    // Cancel any previous search
                    searchJob?.cancel()

                    when {
                        query.isBlank() -> {
                            println("LocationViewModel: Query is blank, clearing results")
                            clearSearchResults()
                        }

                        query.length < 2 -> {
                            println("LocationViewModel: Query too short (${query.length} chars), skipping search")
                            updateState(predictions = emptyList())
                        }

                        else -> {
                            searchJob = viewModelScope.launch {
                                searchPlace(query)
                            }
                        }
                    }
                }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.UpdateSearchQuery -> {
                println("LocationViewModel: Updating search query to '${action.query}'")
                updateState(query = action.query)
                println("LocationViewModel: State updated, current query = '${_state.value.query}'") // Add this
            }

            is Action.OnBackClick -> NavigationProvider.navController.popBackStack()


            is Action.OnAddressChange -> {
                updateState(
                    address = action.address,
                    latitude = action.latitude,
                    longitude = action.longitude
                )
            }

            is Action.OnPlaceSelected -> getPlaceDetails(action.place)

            is Action.ClearSearch -> clearSearchResults()

            is Action.RetrySearch -> {
                val currentQuery = _state.value.query.trim()
                if (currentQuery.isNotBlank() && currentQuery.length >= 2) {
                    searchPlace(currentQuery)
                }
            }
        }
    }

    private fun getPlaceDetails(prediction: PlacePrediction) {
//        println("LocationViewModel: Fetching place details for ${prediction.primaryText}")

        viewModelScope.launch {
            updateState(isLoading = true)

            try {
                addressRepository.fetchPlaceDetails(
                    prediction.placeId,
                    APIKEY.PLACE_API_KEY,
                    prediction.sessionToken
                ).collect { result ->
                    when (result) {
                        is AppResult.Success -> {
                            println("LocationViewModel: Place details fetched successfully")
                            updateState(
                                address = result.data.address,
                                latitude = result.data.latitude,
                                longitude = result.data.longitude,
                            )

                            val jsonData = AppJson.encodeToString(
                                AddressData.serializer(),
                                AddressData(
                                    address = result.data.address,
                                    lat = result.data.latitude,
                                    lng = result.data.longitude,
                                )
                            )


                            NavigationProvider.navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("newAddressDeal", jsonData)
                            NavigationProvider.navController.popBackStack()
                        }

                        is AppResult.Error -> {
                            println("LocationViewModel: Error fetching place details: ${result.error}")
                            updateState(
                                error = "Failed to get location details. Please try again."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("LocationViewModel: Exception in getPlaceDetails: ${e.message}")
                updateState(
                    error = "Something went wrong. Please try again."
                )
            }
        }
    }

    private fun searchPlace(query: String) {
        println("LocationViewModel: Starting search for '$query'")

        // Cancel any previous search
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            updateState(isLoading = true)

            try {
                addressRepository.fetchPlacePredictions(query, apiKey = APIKEY.PLACE_API_KEY)
                    .catch { exception ->
                        println("LocationViewModel: Search error: ${exception.message}")
                        exception.printStackTrace()

                        // Emit error state
                        updateState(
                            isLoading = false,
                            error = when {
                                exception.message?.contains("network", ignoreCase = true) == true ->
                                    "Network error. Please check your connection."

                                exception.message?.contains("quota", ignoreCase = true) == true ->
                                    "Service temporarily unavailable. Please try again later."

                                else -> "Search failed. Please try again."
                            },
                            predictions = emptyList()
                        )
                    }
                    .collect { predictions ->
                        println("LocationViewModel: Search completed with ${predictions.size} results")

                        updateState(
                            predictions = predictions
                        )

                        // Log empty results for debugging
                        if (predictions.isEmpty()) {
                            println("LocationViewModel: No results found for query '$query'")
                        }
                    }
            } catch (e: Exception) {
                println("LocationViewModel: Unexpected error in searchPlace: ${e.message}")
                e.printStackTrace()

                updateState(
                    error = "Search failed. Please try again.",
                    predictions = emptyList()
                )
            }
        }
    }

    private fun clearSearchResults() {
        println("LocationViewModel: Clearing search results")
        searchJob?.cancel()
        updateState(
            query = "",
            predictions = emptyList(),
        )
    }

    private fun updateState(
        query: String = _state.value.query,
        address: String? = _state.value.address,
        latitude: Double? = _state.value.latitude,
        longitude: Double? = _state.value.longitude,
        isLoading: Boolean = false,
        predictions: List<PlacePrediction> = _state.value.predictions,
        error: String? = _state.value.error
    ) {
        _state.update {
            it.copy(
                query = query,
                address = address,
                latitude = latitude,
                longitude = longitude,
                isLoading = isLoading,
                predictions = predictions,
                error = error
            )
        }
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        println("LocationViewModel: Cleaned up")
    }

    data class UiState(
        val query: String = "",
        val address: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isLoading: Boolean = false,
        val predictions: List<PlacePrediction> = emptyList(),
        val error: String? = null
    )

    sealed interface Action {
        data class UpdateSearchQuery(val query: String) : Action

        data class OnAddressChange(
            val address: String,
            val latitude: Double,
            val longitude: Double
        ) : Action

        data class OnPlaceSelected(val place: PlacePrediction) : Action

        data object OnBackClick : Action
        data object ClearSearch : Action
        data object RetrySearch : Action
    }
}