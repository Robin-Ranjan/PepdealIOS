package com.pepdeal.infotech.placeAPI

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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

val jsonParser = Json { ignoreUnknownKeys = true }

@Serializable
data class PlacePrediction(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val area: String = "",
    val state: String = "",
    val sessionToken: String = ""
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

// ✅ Data Model for API Response
@Serializable
data class PlaceDetailsResponse(
    val result: PlaceResult?
)

@Serializable
data class PlaceResult(
    val name: String?,

    @SerialName("formatted_address")
    val formattedAddress: String?,

    val geometry: Geometry?,

    @SerialName("address_components")
    val addressComponents: List<AddressComponent>?
)

@Serializable
data class Geometry(
    val location: Location
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class AddressComponent(
    @SerialName("long_name")
    val longName: String,

    val types: List<String>
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

// ✅ Function to Fetch Place Details
suspend fun fetchPlaceDetails(
    placeId: String,
    apiKey: String,
    sessionToken: String
): PlacePrediction? {
    val url = "https://maps.googleapis.com/maps/api/place/details/json" +
            "?place_id=$placeId&key=$apiKey&sessiontoken=$sessionToken&fields=name,formatted_address,geometry/location,address_components"

    return try {
        val response: HttpResponse = httpClient.get(url)
        if (response.status == HttpStatusCode.OK) {
            val jsonResponse = response.bodyAsText()
            val detailsResponse: PlaceDetailsResponse = jsonParser.decodeFromString(jsonResponse)

            val location = detailsResponse.result?.geometry?.location
            val addressComponents = detailsResponse.result?.addressComponents

            var city: String? = null
            var area: String? = null
            var state: String? = null

            addressComponents?.forEach { component ->
                when {
                    "locality" in component.types -> city = component.longName // City
                    "sublocality" in component.types -> area = component.longName // Area
                    "administrative_area_level_1" in component.types -> state =
                        component.longName // State
                }
            }

            location?.let {
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
            }
        } else {
            println("HTTP Error: ${response.status}")
            null
        }
    } catch (e: Exception) {
        println("Error fetching place details: ${e.message}")
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
