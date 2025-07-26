package com.pepdeal.infotech.product

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import kotlinx.coroutines.flow.Flow

interface ListAllProductRepository {
    suspend fun fetchAllProductOfShop(shopId: String): Flow<AppResult<List<ProductWithImages>, DataError.Remote>>
    suspend fun updateProductStatusByShopOwner(
        productId: String,
        status: String,
    ): Flow<AppResult<Unit, DataError.Remote>>
}