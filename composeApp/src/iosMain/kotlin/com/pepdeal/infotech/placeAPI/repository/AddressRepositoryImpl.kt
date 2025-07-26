package com.pepdeal.infotech.placeAPI.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.placeAPI.PlaceDetailsResponse
import com.pepdeal.infotech.placeAPI.PlacePrediction
import com.pepdeal.infotech.placeAPI.PredictionResponse
import com.pepdeal.infotech.placeAPI.encode
import com.pepdeal.infotech.placeAPI.jsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import platform.Foundation.NSUUID

class AddressRepositoryImpl(private val httpClient: HttpClient) : AddressRepository {
    override suspend fun fetchPlacePredictions(
        query: String,
        apiKey: String
    ): Flow<List<PlacePrediction>> = flow {
        val sessionToken = NSUUID().UUIDString()
        val encodedQuery = withContext(Dispatchers.IO) {
            encode(query)
        }

        val url =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$encodedQuery&key=$apiKey&sessiontoken=$sessionToken&components=country:IN"
        emit(
            try {
                val response: HttpResponse = httpClient.get(url)
                if (response.status == HttpStatusCode.OK) {
                    val predictionResponse: PredictionResponse = response.body()
                    predictionResponse.predictions.map { place ->
                        PlacePrediction(
                            name = place.description,
                            address = place.description,
                            placeId = place.placeId,
                            sessionToken = sessionToken
                        )
                    }
                } else {
                    println("HTTP Error: ${response.status}")
                    emptyList()
                }
            } catch (e: Exception) {
                println("Network Error: ${e.message}")
                emptyList()
            }
        )
    }

    override suspend fun fetchPlaceDetails(
        placeId: String,
        apiKey: String,
        sessionToken: String
    ): Flow<AppResult<PlacePrediction, DataError.Remote>> = flow {
        val url = "https://maps.googleapis.com/maps/api/place/details/json" +
                "?place_id=$placeId&key=$apiKey&sessiontoken=$sessionToken&fields=name,formatted_address,geometry/location,address_components"

        try {
            val response: HttpResponse = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                val jsonResponse = response.bodyAsText()
                val detailsResponse: PlaceDetailsResponse =
                    jsonParser.decodeFromString(jsonResponse)

                val location = detailsResponse.result?.geometry?.location
                val addressComponents = detailsResponse.result?.addressComponents

                var city: String? = null
                var area: String? = null
                var state: String? = null

                addressComponents?.forEach { component ->
                    when {
                        "locality" in component.types -> city = component.longName
                        "sublocality" in component.types -> area = component.longName
                        "administrative_area_level_1" in component.types -> state =
                            component.longName
                    }
                }

                location?.let {
                    emit(
                        AppResult.Success(
                            PlacePrediction(
                                placeId = placeId,
                                name = detailsResponse.result.name ?: "",
                                address = detailsResponse.result.formattedAddress ?: "",
                                latitude = it.lat,
                                longitude = it.lng,
                                city = city ?: "",
                                area = area ?: "",
                                state = state ?: "",
                                sessionToken = sessionToken
                            )
                        )
                    )
                }
            } else {
                println("HTTP Error: ${response.status}")
                emit(AppResult.Error(DataError.Remote(DataError.RemoteType.SERVER, "Server Error")))
            }
        } catch (e: Exception) {
            println("Error fetching place details: ${e.message}")
            emit(AppResult.Error(DataError.Remote(DataError.RemoteType.SERVER, "Server Error")))
        }
    }


}