package com.pepdeal.infotech.product.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.ProductUiDto
import com.pepdeal.infotech.product.ShopItems
import kotlinx.coroutines.flow.Flow

interface ProductSearchRepository {
    fun getAllProductsSearchFlowPagination(
        userId: String?,
        startIndex: String?,
        pageSize: Int,
        searchQuery: String = ""
    ): Flow<AppResult<List<ShopItems>, DataError.Remote>>
}