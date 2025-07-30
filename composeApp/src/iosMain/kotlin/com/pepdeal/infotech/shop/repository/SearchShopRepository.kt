package com.pepdeal.infotech.shop.repository

import com.pepdeal.infotech.shop.modal.ShopMaster
import kotlinx.coroutines.flow.Flow

interface SearchShopRepository {
    suspend fun getActiveSearchedShopsFlowPagination(
        lastShopId: String? = null,
        pageSize: Int = 200,
        searchQuery: String = ""
    ): Flow<List<ShopMaster>>
}

