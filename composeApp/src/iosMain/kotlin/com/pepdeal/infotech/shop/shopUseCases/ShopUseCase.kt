package com.pepdeal.infotech.shop.shopUseCases

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.shop.repository.SearchShopRepository
import com.pepdeal.infotech.shop.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

class ShopUseCase(
    private val shopRepository: ShopRepository,
    private val productRepository: ProductRepository,
    private val shopSearchRepository: SearchShopRepository,
) {
    fun getShop(
        userLat: Double,
        userLng: Double
    ): Flow<AppResult<ShopWithProducts, DataError.Remote>> = channelFlow {
        try {
            shopRepository.getNearbyActiveShopsFlow(userLat, userLng)
                .collect { shopResult ->
                    when (shopResult) {
                        is AppResult.Success -> {
                            val shop = shopResult.data
                            val shopId = shop?.shopId

                            if (shopId?.isNotBlank() == true && shopId.isNotEmpty()) {
                                val productResult =
                                    productRepository.getActiveProductsWithImages(shopId)

                                if (productResult is AppResult.Success && productResult.data.isNotEmpty()) {
                                    println("✅ Emitting ShopWithProducts: ${shop.shopName} - ${productResult.data.size} products")
                                    send(
                                        AppResult.Success(
                                            ShopWithProducts(
                                                shop,
                                                productResult.data
                                            )
                                        )
                                    )
                                } else {
                                    println("⚠️ Skipping shop ${shop.shopName} due to empty or invalid product list")
                                }
                            } else {
                                println("❌ Invalid shopId: ${shop?.shopId}")
                            }
                        }

                        is AppResult.Error -> {
                            println("❌ Shop fetch error: ${shopResult.error.message}")
                            send(AppResult.Error(shopResult.error))
                        }
                    }
                }
        } catch (e: Exception) {
            println("❌ Exception in getShop(): ${e.message}")
            send(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.EMPTY_RESULT,
                        message = e.message
                    )
                )
            )
        }
    }

    fun searchShop(
        lastShopId: String?,
        pageSize: Int,
        searchQuery: String
    ): Flow<ShopWithProducts> = flow {
        try {
            shopSearchRepository.getActiveSearchedShopsFlowPagination(
                lastShopId = lastShopId,
                pageSize = pageSize,
                searchQuery = searchQuery
            ).collect { shops->
                for (shop in shops) {
                    val shopId = shop.shopId
                    if (!shopId.isNullOrBlank()) {
                        val productsResult = productRepository.getActiveProductsWithImages(shopId)
                        if (productsResult is AppResult.Success && productsResult.data.isNotEmpty()) {
                            println("✅ Emitting: ${shop.shopName} with ${productsResult.data.size} products")
                            emit(ShopWithProducts(shop, productsResult.data))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Exception in searchShop(): ${e.message}")
        }
    }
}