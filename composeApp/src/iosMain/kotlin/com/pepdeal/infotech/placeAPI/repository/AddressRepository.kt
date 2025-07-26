package com.pepdeal.infotech.placeAPI.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.placeAPI.PlacePrediction
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    suspend fun fetchPlacePredictions(query: String, apiKey: String): Flow<List<PlacePrediction>>

    suspend fun fetchPlaceDetails(
        placeId: String,
        apiKey: String,
        sessionToken: String
    ): Flow<AppResult<PlacePrediction, DataError.Remote>>
}