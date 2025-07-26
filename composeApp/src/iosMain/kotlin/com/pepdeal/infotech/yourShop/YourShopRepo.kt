package com.pepdeal.infotech.yourShop

import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext

class YourShopRepo {
    suspend fun fetchShopDetails(shopId: String): ShopMaster? {
        return try {
            ShopDetailsRepo().fetchShopDetails(shopId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getActiveProductsWithImages(shopId: String): Flow<List<ProductWithImages>> = channelFlow {
        ShopDetailsRepo().getActiveProductsWithImages(shopId = shopId)
    }

    // to get the shop Id by using shoId to show in shop Details page
    suspend fun fetchShopServices(shopId: String): ShopStatusMaster? = withContext(Dispatchers.IO) {
        return@withContext try {
            ShopDetailsRepo().fetchShopServices(shopId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}