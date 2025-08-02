package com.pepdeal.infotech.product.repository

import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.product.SearchProductTagSummaryResult
import com.pepdeal.infotech.shop.repository.AlgoliaSearchResponse
import com.pepdeal.infotech.shop.repository.HighlightField
import com.pepdeal.infotech.shop.repository.HighlightTag
import com.pepdeal.infotech.shop.repository.parseSearchTags
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

class AlgoliaProductSearchTagRepositoryImpl(
    private val httpClient: HttpClient
) : AlgoliaProductSearchTagRepository {

    private val appId = "7TI0X2HETC"
    private val apiKey = "a74610f93f0298f57be5ea19a9536c0e"
    private val indexName = "product searchTag indexing"

    suspend fun getSearchByAlgolia(searchQuery: String): AlgoliaSearchResponse<ProductSearchHit>? {
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
                    ProductSearchHit.serializer()
                ), responseBody
            )

            return searchResponse
        } catch (e: Exception) {
            println("Request failed: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    override suspend fun searchProductTag(query: String): Result<List<ProductSearchTagResult>> {
        return try {

            val requestBody = mapOf(
                "params" to listOf(
                    "query=$query",
                    "hitsPerPage=10",
                    "attributesToHighlight=searchTag,productName"
                ).joinToString("&")
            )

            val encodedRequest = AppJson.encodeToString(requestBody)


            val response: String =
                httpClient.post("https://${appId}-dsn.algolia.net/1/indexes/${indexName.encodeURLPath()}/query") {
                    contentType(ContentType.Application.Json)
                    header("X-Algolia-Application-Id", appId)
                    header("X-Algolia-API-Key", apiKey)
                    setBody(encodedRequest)
                }.bodyAsText()

            val json = AppJson.parseToJsonElement(response).jsonObject
            val hits = json["hits"]?.jsonArray ?: JsonArray(emptyList())

            val results = hits.map { element ->
                val obj = element.jsonObject

                ProductSearchTagResult(
                    objectID = obj["objectID"]?.jsonPrimitive?.content ?: "",
                    path = obj["path"]?.jsonPrimitive?.contentOrNull ?: "",
                    searchTag = parseSearchTags(obj["searchTag"]),
                    productName = obj["productName"]?.jsonPrimitive?.contentOrNull ?: "",
                    geoHash = obj["geoHash"]?.jsonPrimitive?.contentOrNull ?: "",
                    lastmodified = obj["lastmodified"]?.jsonPrimitive?.longOrNull ?: 0L,
                    highlightResult = hits.firstOrNull()?.jsonObject?.get("highlightResult")?.jsonObject
                )
            }

            Result.success(results)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun searchBestProducts(query: String): List<String> {
        val response = getSearchByAlgolia(query)
        return response?.hits
            ?.filter { it.getPerfectMatchCount() > 0 }
            ?.sortedByDescending { it.getPerfectMatchCount() }
            ?.map { it.getProductId() }
            .orEmpty()
    }

    override suspend fun searchSummary(query: String): Flow<Result<SearchProductTagSummaryResult>> =
        flow {
            println("🟡 Starting search flow with query: '$query'")

            val result = searchProductTag(query)
            println("🔵 Result from searchProductTag: isSuccess = ${result.isSuccess}")

            if (result.isSuccess) {
                val results = result.getOrNull().orEmpty()
                println("🟢 Number of product results: ${results.size}")

                val allTags = results.flatMap { it.getHighlightedSearchTags() }
                println("🟣 Extracted ${allTags.size} tags from products: $allTags")

                val topTags = allTags
                    .groupingBy { it.lowercase() }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .take(5)

                println("✅ Top Tags: $topTags")

                val topShops = results
                    .map { it.productName }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sortedByDescending { it.commonPrefixWith(query, ignoreCase = true).length }
                    .take(5)

                println("✅ Top Shops (based on product names): $topShops")

                emit(Result.success(SearchProductTagSummaryResult(topTags, topShops)))
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error during search")
                println("❌ Search failed: ${exception.message}")
                emit(Result.failure(exception))
            }
        }


    fun ProductSearchHit.getPerfectMatchCount(): Int {
        return highlightProductSearchResult?.searchTag
            ?.count { it.matchLevel == "full" || it.fullyHighlighted }
            ?: 0
    }
}

fun ProductSearchHit.getProductId(): String {
    return path.substringAfter("product_master/", "")
}

@Serializable
data class ProductSearchHit(
    val objectID: String,
    val productName: String,
    val path: String,
    val searchTag: List<String> = emptyList(),
    val geoHash: String = "",
    val lastmodified: Long? = null,
    @SerialName("_highlightResult")
    val highlightProductSearchResult: HighlightProductSearchResult? = null
)

@Serializable
data class HighlightProductSearchResult(
    val searchTag: List<HighlightTag> = emptyList(),
    val productName: HighlightField? = null,
    val path: HighlightField? = null,
    val geoHash: HighlightField? = null
)