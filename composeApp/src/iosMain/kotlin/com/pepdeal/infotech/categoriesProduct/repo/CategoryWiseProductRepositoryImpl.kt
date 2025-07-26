package com.pepdeal.infotech.categoriesProduct.repo

import com.pepdeal.infotech.categoriesProduct.viewModel.CategoryWiseProductViewModal
import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.favourite_product.repository.FavouriteProductRepository
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.util.Util.toNameFormat
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CategoryWiseProductRepositoryImpl(
    private val httpClient: HttpClient,
    private val productRepo: ProductRepository,
    private val favouritesRepo: FavouriteProductRepository
) : CategoryWiseProductRepository {

    override suspend fun getTheCategoryWiseProduct(
        subCategoryName: String,
        userId: String?
    ): Flow<AppResult<CategoryWiseProductViewModal.CategoryWiseProductModel, DataError.Remote>> =
        flow {
            val nameFormatSubCategory = subCategoryName.toNameFormat().trim()
            println("Fetching products for subCategory: $nameFormatSubCategory")

            try {
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.PRODUCT_MASTER,
                    filters = listOf(
                        FirestoreFilter("subCategoryId", nameFormatSubCategory),
                        FirestoreFilter("productActive", "0"),
                        FirestoreFilter("flag", "0"),
                        FirestoreFilter("shopActive", "0"),
                        FirestoreFilter("shopBlock", "0"),
                    ),
                    limit = 200
                )

                val queryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                    safeCall {
                        httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                            contentType(ContentType.Application.Json)
                            setBody(queryBody)
                        }
                    }

                if (queryResponse is AppResult.Error) {
                    println("❌ Failed to fetch products.")
                    emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER)))
                }

                if (queryResponse is AppResult.Success) {
                    val validProduct = queryResponse.data.mapNotNull { result ->
                        val fields = result.document?.fields ?: return@mapNotNull null
                        ProductMaster(
                            productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            productName = (fields["productName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            brandId = (fields["brandId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            brandName = (fields["brandName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            categoryId = (fields["categoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            subCategoryId = (fields["subCategoryId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description = (fields["description"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            description2 = (fields["description2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            specification = (fields["specification"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            warranty = (fields["warranty"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            sizeId = (fields["sizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            sizeName = (fields["sizeName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            color = (fields["color"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            onCall = (fields["onCall"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mrp = (fields["mrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            discountMrp = (fields["discountMrp"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            sellingPrice = (fields["sellingPrice"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            productActive = (fields["productActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopBlock = (fields["shopBlock"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopLongitude = (fields["shopLongitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            shopLatitude = (fields["shopLatitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }

                    if (validProduct.isEmpty()) {
                        emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                    }

                    for (product in validProduct) {
                        product?.let {

                            val imageResult = productRepo.fetchProductImages(product.productId)
                            val image =
                                if (imageResult is AppResult.Success) imageResult.data else null
                            val isFavourite = userId?.let {
                                favouritesRepo.isFavorite(
                                    productId = product.productId,
                                    userId = it
                                )
                            }?.getOrDefault(false) ?: false

                            val shopItem = ShopItems(
                                shopId = product.shopId,
                                productId = product.productId,
                                productName = product.productName,
                                sellingPrice = product.sellingPrice,
                                mrp = product.mrp,
                                description = product.description,
                                category = product.categoryId,
                                discountMrp = product.discountMrp,
                                productActive = product.productActive,
                                flag = product.flag,
                                subCategoryId = product.subCategoryId,
                                searchTag = product.searchTag,
                                onCall = product.onCall,
                                createdAt = product.createdAt,
                                updatedAt = product.updatedAt,
                                shopActive = product.shopActive,
                                shopBlock = product.shopBlock,
                                shopLongitude = product.shopLongitude,
                                shopLatitude = product.shopLatitude,
                                image = image?.firstOrNull()?.productImages.orEmpty(),
                            )

                            emit(
                                AppResult.Success(
                                    CategoryWiseProductViewModal.CategoryWiseProductModel(
                                        shopItem,
                                        isFavourite
                                    )
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error fetching products: ${e.message}")
                e.printStackTrace()
                emit(AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER)))
            }
        }.flowOn(Dispatchers.IO)
}