package com.pepdeal.infotech.shop.repository

import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.shop.viewModel.SearchShopTagSummaryResult
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlin.collections.orEmpty

class AlgoliaShopSearchTagRepositoryImpl(
    private val httpClient: HttpClient,
) : AlgoliaShopSearchTagRepository {
    private val appId = "7TI0X2HETC"
    private val apiKey = "a74610f93f0298f57be5ea19a9536c0e"
    private val indexName = "seachTag and geoHash Index"

    suspend fun getSearchByAlgolia(searchQuery: String): AlgoliaSearchResponse<ShopSearchHit>? {


        try {
            val response: HttpResponse =
                httpClient.post("https://$appId-dsn.algolia.net/1/indexes/${indexName.encodeURLPath()}/query") {
                    contentType(ContentType.Application.Json)
                    header("X-Algolia-Application-Id", appId)
                    header("X-Algolia-API-Key", apiKey)
                    setBody(buildJsonObject {
                        put("query", searchQuery)
                        put("hitsPerPage", 60)
                    })
                }

            if (response.status.value !in 200..299) {
                return null
            }

            val responseBody = response.bodyAsText()

            val searchResponse = AppJson.decodeFromString(
                AlgoliaSearchResponse.serializer(
                    ShopSearchHit.serializer()
                ), responseBody
            )

            return searchResponse
        } catch (e: Exception) {
            println("Request failed: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override suspend fun searchBestShops(query: String): List<String> {
        val response = getSearchByAlgolia(query)

        return response?.hits
            ?.filter { it.getPerfectMatchCount() > 0 }
            ?.sortedByDescending { it.getPerfectMatchCount() }
            ?.map { it.getShopId() }
            .orEmpty()
    }

    override suspend fun searchSummary(query: String): Flow<Result<SearchShopTagSummaryResult>> =
        flow {
            val result = searchShopTags(query)

            if (result.isSuccess) {
                val results = result.getOrNull().orEmpty()

                // Extract all highlighted search tags
                val allTags = results.flatMap { it.getHighlightedSearchTags() }

                val topTags = allTags
                    .groupingBy { it.lowercase() }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .take(5)

                // Extract top shop names using highlight
                val topShops = results
                    .map { it.shopName }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sortedByDescending { it.commonPrefixWith(query, ignoreCase = true).length }
                    .take(5)

                emit(Result.success(SearchShopTagSummaryResult(topTags, topShops)))
            } else {
                emit(
                    Result.failure(
                        result.exceptionOrNull() ?: Exception("Unknown error during search")
                    )
                )
            }
        }

    override suspend fun searchShopTags(query: String): Result<List<ShopSearchTagResult>> {
        return try {
            println("🔍 searchShopTags() called with query: $query")

            val requestBody = mapOf(
                "params" to listOf(
                    "query=$query",
                    "hitsPerPage=10",
                    "attributesToHighlight=searchTag,shopName"
                ).joinToString("&")
            )

            val encodedRequest = AppJson.encodeToString(requestBody)
            println("📦 Encoded request body: $encodedRequest")


            val response: String =
                httpClient.post("https://${appId}-dsn.algolia.net/1/indexes/${indexName.encodeURLPath()}/query") {
                    contentType(ContentType.Application.Json)
                    header("X-Algolia-Application-Id", appId)
                    header("X-Algolia-API-Key", apiKey)
                    setBody(encodedRequest)
                }.bodyAsText()

            println("📩 Raw response: $response")

            val json = AppJson.parseToJsonElement(response).jsonObject
            val hits = json["hits"]?.jsonArray ?: JsonArray(emptyList())

            println("📊 Total hits: ${hits.size}")

            val results = hits.map { element ->
                val obj = element.jsonObject

                ShopSearchTagResult(
                    objectID = obj["objectID"]?.jsonPrimitive?.content ?: "",
                    path = obj["path"]?.jsonPrimitive?.contentOrNull ?: "",
                    searchTag = parseSearchTags(obj["searchTag"]),
                    shopName = obj["shopName"]?.jsonPrimitive?.contentOrNull ?: "",
                    geoHash = obj["geoHash"]?.jsonPrimitive?.contentOrNull ?: "",
                    lastmodified = obj["lastmodified"]?.jsonPrimitive?.longOrNull ?: 0L,
                    highlightResult = hits.firstOrNull()?.jsonObject?.get("highlightResult")?.jsonObject
                )
            }

            println("✅ Final results count: ${results.size}")
            Result.success(results)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun ShopSearchHit.getPerfectMatchCount(): Int {
        return highlightResult?.searchTag
            ?.count { it.matchLevel == "full" || it.fullyHighlighted }
            ?: 0
    }

}

fun AlgoliaSearchResponse<ShopSearchHit>.getShopIds(): List<String> {
    return hits.map { it.getShopId() }.filter { it.isNotEmpty() }
}

fun ShopSearchHit.getShopId(): String {
    return path.substringAfter("shop_master/", "")
}


@Serializable
data class AlgoliaSearchResponse<T>(
    val hits: List<T> = emptyList(),
    val nbHits: Int = 0,
    val page: Int = 0,
    val nbPages: Int = 0,
    val hitsPerPage: Int = 0,
    val exhaustiveNbHits: Boolean = true,
    val exhaustiveTypo: Boolean = true,
    val query: String = "",
    val params: String = "",
    val processingTimeMS: Int = 0
)

// Individual hit/result DTO
@Serializable
data class ShopSearchHit(
    val objectID: String,
    val shopName: String,
    val path: String,
    val searchTag: List<String> = emptyList(),
    val geoHash: String = "",
    val lastmodified: Long? = null,
    @SerialName("_highlightResult")
    val highlightResult: HighlightResult? = null
)

@Serializable
data class HighlightResult(
    val searchTag: List<HighlightTag> = emptyList(),
    val shopName: HighlightField? = null,
    val path: HighlightField? = null,
    val geoHash: HighlightField? = null
)

@Serializable
data class HighlightTag(
    val value: String,
    val matchLevel: String,
    val fullyHighlighted: Boolean = false,
    val matchedWords: List<String> = emptyList()
)

@Serializable
data class HighlightField(
    val value: String,
    val matchLevel: String,
    val matchedWords: List<String> = emptyList()
)

fun parseSearchTags(value: Any?): List<String> {
    return when (value) {
        is List<*> -> {
            // If it's already a list, convert each item to string
            value.mapNotNull { it?.toString() }
        }

        is String -> {
            // If it's a single string, wrap it in a list
            listOf(value)
        }

        is Array<*> -> {
            // If it's an array, convert to list of strings
            value.mapNotNull { it?.toString() }
        }

        null -> {
            // If it's null, return empty list
            emptyList()
        }

        else -> {
            // For any other type, try to convert to string and wrap in list
            listOf(value.toString())
        }
    }
}