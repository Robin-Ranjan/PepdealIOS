package com.pepdeal.infotech.product.productUseCases

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.product.ProductUiDto
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.product.repository.ProductSearchRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ProductUseCase(
    private val favRepo: FavouriteProductRepository,
    private val productRepository: ProductRepository,
    private val productSearchRepository: ProductSearchRepository
) {
    fun fetchedProduct(
        userId: String?,
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 2.0
    ): Flow<AppResult<ProductUiDto, DataError.Remote>> = channelFlow {
        try {
            val productListResult = productRepository.getNearByProducts(
                userLat = userLat,
                userLng = userLng,
                radiusKm = radiusKm
            ).first()

            if (productListResult is AppResult.Success) {
                val productList = productListResult.data

                coroutineScope {
                    productList.map { product ->
                        launch {
                            var image = ""
                            productRepository
                                .fetchProductImages(product.productId, count = 1)
                                .onSuccess { image = it?.firstOrNull()?.productImages ?: "" }

                            val isFav = userId?.let {
                                favRepo.isFavorite(it, product.productId).isSuccess
                            } ?: false

                            send(
                                AppResult.Success(
                                    ProductUiDto(
                                        product.copy(image = image),
                                        isFavourite = isFav
                                    )
                                )
                            )
                        }
                    }.joinAll()
                }
            }
        } catch (e: Exception) {
            println("❌ General error: ${e.message}")
            send(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
        }
    }

    suspend fun searchProduct(
        userId: String?,
        startIndex: String?,
        pageSize: Int,
        searchQuery: String
    ): Flow<AppResult<ProductUiDto, DataError.Remote>> = channelFlow {
        try {
            val productListResult = productSearchRepository.getAllProductsSearchFlowPagination(
                userId = userId,
                startIndex = startIndex,
                pageSize = pageSize,
                searchQuery = searchQuery
            ).first()

            if (productListResult is AppResult.Success) {
                val productList = productListResult.data

                coroutineScope {
                    productList.map { product ->
                        launch {
                            var image = ""
                            productRepository
                                .fetchProductImages(product.productId, count = 1)
                                .onSuccess { image = it?.firstOrNull()?.productImages ?: "" }

                            val isFav = userId?.let {
                                favRepo.isFavorite(it, product.productId).isSuccess
                            } ?: false

                            send(
                                AppResult.Success(
                                    ProductUiDto(
                                        product.copy(image = image),
                                        isFavourite = isFav
                                    )
                                )
                            )
                        }
                    }.joinAll()
                }
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
}