package com.pepdeal.infotech.product.productUseCases

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.product.ProductUiDto
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.producrDetails.ProductDetailUiModel
import com.pepdeal.infotech.product.repository.AlgoliaProductSearchTagRepository
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.repository.ShopRepository
import com.pepdeal.infotech.tickets.domain.TicketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductUseCase(
    private val favRepo: FavouriteProductRepository,
    private val productRepository: ProductRepository,
    private val ticketRepository: TicketRepository,
    private val shopRepository: ShopRepository,
    private val productAlgoliaRepository: AlgoliaProductSearchTagRepository
) {
    fun fetchedProducts(
        userId: String?,
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 2.0
    ): Flow<AppResult<ProductUiDto, DataError.Remote>> = channelFlow {
        try {
            println("📡 Fetching nearby products within $radiusKm km of ($userLat, $userLng)")
            if (userId == null) {
                println("⚠️ No userId available – favorites will be marked as false.")
            }

            val productListResult = productRepository.getNearByProducts(
                userLat = userLat,
                userLng = userLng,
                radiusKm = radiusKm
            ).first()

            if (productListResult is AppResult.Success) {
                val productList = productListResult.data
                println("✅ Fetched ${productList.size} nearby products")

                productList.map { product ->
                    launch {
                        try {
                            var image = ""
                            productRepository.fetchProductImages(product.productId, count = 1)
                                .onSuccess {
                                    image = it?.firstOrNull()?.productImages.orEmpty()
                                    println("🖼️ Image loaded for ${product.productId}")
                                }
                                .onError {
                                    println("⚠️ Error loading image for ${product.productId}: ${it.message}")
                                }

                            val isFav = userId?.let { uid ->
                                println("🔍 Checking favorite for uid=$uid, pid=${product.productId}")
                                val favResult = favRepo.isFavorite(uid, product.productId)
                                println("❤️ Favorite check result: ${favResult.isSuccess}")
                                favResult.getOrElse {
                                    println("❌ Error checking favorite for ${product.productId}: ${it.message}")
                                    false
                                }
                            } ?: false

                            val productDto = ProductUiDto(
                                shopItem = product.copy(image = image),
                                isFavourite = isFav
                            )

                            // Ensure `send()` is not called in concurrent context
                            withContext(coroutineContext) {
                                send(AppResult.Success(productDto))
                            }

                        } catch (e: Exception) {
                            println("❌ Exception while processing product ${product.productId}: ${e.message}")
                        }
                    }
                }.joinAll()

            } else {
                println("❌ Failed to fetch nearby product list.")
                send(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.UNKNOWN,
                            message = "Failed to load nearby products"
                        )
                    )
                )
            }

        } catch (e: Exception) {
            println("❌ General error in fetchedProducts: ${e.message}")
            send(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = e.message
                    )
                )
            )
        }
    }

    fun searchProduct(
        userId: String?,
        searchQuery: String
    ): Flow<AppResult<ProductUiDto, DataError.Remote>> = channelFlow {
        try {
            val productIds = productAlgoliaRepository.searchBestProducts(query = searchQuery)

            coroutineScope {
                productIds
                    .filter { it.isNotBlank() }
                    .map { productId ->
                        async {
                            val shopItem = productRepository.getShopItem(productId)
                            if (shopItem != null) {
                                var image = ""
                                productRepository
                                    .fetchProductImages(shopItem.productId, count = 1)
                                    .onSuccess { image = it?.firstOrNull()?.productImages ?: "" }

                                val isFav = userId?.let { uid ->
                                    favRepo.isFavorite(uid, productId).getOrElse {
                                        println("❌ Favorite check failed for $productId: ${it.message}")
                                        false
                                    }
                                } ?: false

                                send(
                                    AppResult.Success(
                                        ProductUiDto(
                                            shopItem.copy(image = image),
                                            isFavourite = isFav
                                        )
                                    )
                                )
                            }
                        }
                    }.awaitAll()
            }
        } catch (e: Exception) {
            println("❌ General error: ${e.message}")
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

    fun fetchProductDetail(
        productId: String,
        userId: String?
    ): Flow<AppResult<ProductDetailUiModel, DataError.Remote>> = flow {
        try {
            val productResult = productRepository.fetchProductDetails(productId)
            val productImagesResult = productRepository.fetchProductImages(productId, 3)
            val isFavourite =
                userId?.let { favRepo.isFavorite(it, productId).getOrElse { false } } ?: false

            if (productResult is AppResult.Success && productImagesResult is AppResult.Success) {
                val product = productResult.data
                val images = productImagesResult.data

                if (product != null && images != null) {
                    when (val shopResult = shopRepository.fetchShopDetails(product.shopId)) {
                        is AppResult.Success -> {
                            val shop = shopResult.data
                            if (shop != null) {
                                // Check for active ticket (approved or waiting)
                                val isTicketActive = userId?.let {
                                    when (ticketRepository.checkTicketExists(
                                        shopId = product.shopId,
                                        productId = product.productId,
                                        userId = userId
                                    )) {
                                        is AppResult.Success -> true  // Active ticket exists
                                        is AppResult.Error -> false    // No active ticket
                                    }
                                } ?: false

                                val result = ProductDetailUiModel(
                                    product = ProductWithImages(product, images),
                                    shop = shop,
                                    isFavourite = isFavourite,
                                    isTicketActive = isTicketActive
                                )

                                println("ProductDetails:- $result")

                                emit(AppResult.Success(result))
                            } else {
                                emit(
                                    AppResult.Error(
                                        DataError.Remote(
                                            type = DataError.RemoteType.EMPTY_RESULT,
                                            message = "Shop not found"
                                        )
                                    )
                                )
                            }
                        }

                        is AppResult.Error -> {
                            emit(AppResult.Error(shopResult.error))
                        }
                    }
                } else {
                    emit(
                        AppResult.Error(
                            DataError.Remote(
                                type = DataError.RemoteType.EMPTY_RESULT,
                                message = "Failed to fetch product or images"
                            )
                        )
                    )
                }
            } else {
                emit(
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.EMPTY_RESULT,
                            message = "Failed to fetch product or images"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = e.message ?: "Unknown error"
                    )
                )
            )
        }
    }

}