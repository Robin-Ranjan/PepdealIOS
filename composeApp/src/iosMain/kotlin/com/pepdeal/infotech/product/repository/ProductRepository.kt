package com.pepdeal.infotech.product.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductUiDto
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.ShopItems
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun fetchProductDetails(productId: String): AppResult<ProductMaster?, DataError.Remote>
    suspend fun fetchProductImages(
        productId: String,
        count: Int = 1
    ): AppResult<List<ProductImageMaster>?, DataError.Remote>

    suspend fun getActiveProductsWithImages(
        shopId: String,
    ): AppResult<List<ProductWithImages>, DataError.Remote>

    suspend fun getNearByProducts(
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 2.0
    ): Flow<AppResult<List<ShopItems>, DataError.Remote>>

}