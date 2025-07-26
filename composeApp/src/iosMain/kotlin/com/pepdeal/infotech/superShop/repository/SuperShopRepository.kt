package com.pepdeal.infotech.superShop.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.EmptyResult
import com.pepdeal.infotech.superShop.model.SuperShopsWithProduct
import kotlinx.coroutines.flow.Flow

interface SuperShopRepository {
    suspend fun getSuperShopWithProduct(userId: String): Flow<AppResult<SuperShopsWithProduct, DataError.Remote>>
    suspend fun removeSuperShop(
        userId: String,
        shopId: String,
    ): EmptyResult<DataError.Remote>
}