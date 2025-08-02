package com.pepdeal.infotech.product.repository

import com.pepdeal.infotech.product.SearchProductTagSummaryResult
import kotlinx.coroutines.flow.Flow

interface AlgoliaProductSearchTagRepository {
    suspend fun searchProductTag(query: String): Result<List<ProductSearchTagResult>>

    suspend fun searchBestProducts(query: String): List<String>

    suspend fun searchSummary(query: String): Flow<Result<SearchProductTagSummaryResult>>
}

data class ProductSearchTagResult(
    val objectID: String,
    val path: String,
    val searchTag: List<String>,
    val productName: String,
    val geoHash: String,
    val lastmodified: Long,
    val highlightResult: Map<String, Any>? = null
) {
    fun getHighlightedProductName(): String {
        return highlightResult?.get("productName")?.toString()?.removeSurrounding("\"")
            ?: productName
    }

    fun getHighlightedSearchTags(): List<String> {
        val highlighted = highlightResult?.get("searchTag") as? List<*>
        return highlighted?.mapNotNull { it?.toString()?.removeSurrounding("\"") } ?: searchTag
    }
}