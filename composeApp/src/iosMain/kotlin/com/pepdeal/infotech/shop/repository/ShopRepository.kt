package com.pepdeal.infotech.shop.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    suspend fun fetchShopDetails(shopId: String): AppResult<ShopMaster?, DataError.Remote>
    suspend fun getShopMobile(shopId: String): String?
    suspend fun getNearbyActiveShopsFlow(
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 10.0
    ): Flow<AppResult<ShopMaster?, DataError.Remote>>
}