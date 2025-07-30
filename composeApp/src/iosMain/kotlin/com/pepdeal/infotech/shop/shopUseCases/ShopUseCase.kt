package com.pepdeal.infotech.shop.shopUseCases

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.shop.repository.AlgoliaShopSearchTagRepository
import com.pepdeal.infotech.shop.repository.SearchShopRepository
import com.pepdeal.infotech.shop.repository.ShopRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow

class ShopUseCase(
    private val shopRepository: ShopRepository,
    private val productRepository: ProductRepository,
    private val algoliaShopSearchTagRepository: AlgoliaShopSearchTagRepository
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
            val shopIds = algoliaShopSearchTagRepository.searchBestShops(searchQuery)
            println("searchShop called $shopIds")

            coroutineScope {
                val jobs = shopIds
                    .filter { it.isNotBlank() }
                    .map { shopId ->
                        async {
                            val shopResult = shopRepository.fetchShopDetails(shopId)
                            if (shopResult is AppResult.Success && shopResult.data != null) {
                                val shop = shopResult.data
                                val productsResult =
                                    productRepository.getActiveProductsWithImages(shop.shopId!!)
                                if (productsResult is AppResult.Success && productsResult.data.isNotEmpty()) {
                                    println("✅ Emitting: ${shop.shopName} with ${productsResult.data.size} products")
                                    ShopWithProducts(shop, productsResult.data)
                                } else {
                                    println("⚠️ No active products for shop: ${shop.shopName}")
                                    null
                                }
                            } else {
                                println("❌ Failed to fetch shop details for ID: $shopId")
                                null
                            }
                        }
                    }

                // Await all and emit non-null results
                jobs.awaitAll().filterNotNull().forEach { emit(it) }
            }

        } catch (e: Exception) {
            println("❌ Exception in searchShop(): ${e.message}")
            e.printStackTrace()
        }
    }
}