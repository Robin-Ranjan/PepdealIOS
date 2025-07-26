package com.pepdeal.infotech.favourite_product.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.EmptyResult
import com.pepdeal.infotech.favourite_product.modal.FavProductWithImages
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import kotlinx.coroutines.flow.Flow

interface FavouriteProductRepository {
    suspend fun fetchFavoriteProducts(userId: String): Flow<AppResult<FavProductWithImages, DataError.Remote>>
    suspend fun removeFavoriteItem(
        userId: String,
        productId: String,
    ): Flow<AppResult<Unit, DataError.Remote>>

    suspend fun isFavorite(userId: String, productId: String): Result<Boolean>
    suspend fun addFavorite(product: FavoriteProductMaster): EmptyResult<DataError.Remote>
}