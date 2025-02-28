package com.pepdeal.infotech.placeAPI

import com.pepdeal.infotech.util.APIKEY
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSUUID
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters

// Initialize Ktor HttpClient
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

@Serializable
data class PlacePrediction(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val sessionToken: String
)

@Serializable
data class PredictionResponse(
    @SerialName("predictions") val predictions: List<PlacePredictionApiResponse>
)

@Serializable
data class PlacePredictionApiResponse(
    @SerialName("description") val description: String,
    @SerialName("place_id") val placeId: String
)

@Serializable
data class PlaceDetailsResponse(
    @SerialName("result") val result: PlaceDetailsResult?
)

@Serializable
data class PlaceDetailsResult(
    @SerialName("geometry") val geometry: PlaceGeometry?
)

@Serializable
data class PlaceGeometry(
    @SerialName("location") val location: PlaceLocation?
)

@Serializable
data class PlaceLocation(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@OptIn(BetaInteropApi::class)
fun encode(value: String): String {
    return NSString.create(string = value).stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet
    ) ?: value
}

suspend fun fetchPlacePredictions(query: String, apiKey: String): List<PlacePrediction> {
    if (query.isEmpty()) return emptyList()

    val sessionToken = NSUUID().UUIDString()
    val encodedQuery = withContext(Dispatchers.IO) {
        encode(query)
    }

    val url =
        "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$encodedQuery&key=$apiKey&sessiontoken=$sessionToken&components=country:IN"

    return try {
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
}

suspend fun fetchPlaceDetails(
    placeId: String,
    apiKey: String,
    sessionToken: String
): PlacePrediction? {
    val url =
        "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&key=$apiKey&sessiontoken=$sessionToken&fields=geometry/location"

    return try {
        val response: HttpResponse = httpClient.get(url)
        if (response.status == HttpStatusCode.OK) {
            val detailsResponse: PlaceDetailsResponse = response.body()
            val location = detailsResponse.result?.geometry?.location
            location?.let {
                PlacePrediction(
                    placeId = placeId,
                    name = "",
                    address = "",
                    latitude = it.lat,
                    longitude = it.lng,
                    sessionToken = sessionToken
                )
            }
        } else {
            println("HTTP Error: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Exception in fetchPlaceDetails: ${e.message}")
        null
    }
}


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
fun searchAddressFlow(queryFlow: Flow<String>, apiKey: String): Flow<List<PlacePrediction>> {
    return queryFlow
        .debounce(300)
        .distinctUntilChanged()
        .mapLatest { query ->
            if (query.isNotEmpty()) {
                fetchPlacePredictions(query, apiKey)
            } else {
                emptyList()
            }
        }
        .flowOn(Dispatchers.IO)
}
