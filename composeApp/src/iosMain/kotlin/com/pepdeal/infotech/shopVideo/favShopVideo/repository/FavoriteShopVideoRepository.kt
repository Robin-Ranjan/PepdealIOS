package com.pepdeal.infotech.shopVideo.favShopVideo.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.EmptyResult
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.shopVideo.ShopVideoWithShopDetail
import com.pepdeal.infotech.shopVideo.favShopVideo.model.FavouriteShopVideo
import kotlinx.coroutines.flow.Flow

interface FavoriteShopVideoRepository {
    suspend fun getFavoriteShopVideoForUserFlow(userId: String): Flow<AppResult<ShopVideoWithShopDetail?, DataError.Remote>>
    suspend fun removeFavoriteShopVideo(
        userId: String,
        shopId: String
    ): Flow<EmptyResult<DataError.Remote>>

    suspend fun checkIfShopVideoIsFavorite(userId: String, shopId: String): Result<Boolean>

    suspend fun addFavoriteShopVideo(shopVideo: FavouriteShopVideo): EmptyResult<DataError.Remote>
}