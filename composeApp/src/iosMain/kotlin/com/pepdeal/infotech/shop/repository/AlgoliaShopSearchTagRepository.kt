package com.pepdeal.infotech.shop.repository

import com.pepdeal.infotech.shop.viewModel.SearchShopTagSummaryResult
import kotlinx.coroutines.flow.Flow

interface AlgoliaShopSearchTagRepository {
    suspend fun searchShopTags(query: String): Result<List<ShopSearchTagResult>>

    suspend fun searchBestShops(query: String): List<String>

    suspend fun searchSummary(query: String): Flow<Result<SearchShopTagSummaryResult>>
}

data class ShopSearchTagResult(
    val objectID: String,
    val path: String,
    val searchTag: List<String>,
    val shopName: String,
    val geoHash: String,
    val lastmodified: Long,
    val highlightResult: Map<String, Any>? = null
) {

    fun getHighlightedShopName(): String {
        return highlightResult?.get("shopName")?.toString()?.removeSurrounding("\"") ?: shopName
    }

    fun getHighlightedSearchTags(): List<String> {
        val highlighted = highlightResult?.get("searchTag") as? List<*>
        return highlighted?.mapNotNull { it?.toString()?.removeSurrounding("\"") } ?: searchTag
    }
}