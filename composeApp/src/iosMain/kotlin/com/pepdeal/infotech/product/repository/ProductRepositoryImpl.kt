package com.pepdeal.infotech.product.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.FirestoreOperator
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.utils.GeoUtils
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.product.mapper.ProductMapper.toProductMaster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class ProductRepositoryImpl(
    private val httpClient: HttpClient,
) : ProductRepository {

    override suspend fun fetchProductDetails(productId: String): AppResult<ProductMaster?, DataError.Remote> {
        val queryBody = buildFirestoreQuery(
            collection = DatabaseCollection.PRODUCT_MASTER,
            limit = 1,
            filters = listOf(
                FirestoreFilter("productId", productId),
                FirestoreFilter("productActive", "0"),
                FirestoreFilter("flag", "0"),
                FirestoreFilter("shopActive", "0"),
                FirestoreFilter("shopBlock", "0")
            )
        )

        val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
            httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }.body()
        }

        return when (response) {
            is AppResult.Error -> AppResult.Error(response.error)

            is AppResult.Success -> {
                val product = response.data.firstOrNull()?.document?.fields?.let { fields ->
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
                        searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.map { it.stringValue }
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

                AppResult.Success(product)
            }
        }
    }

    override suspend fun fetchProductImages(
        productId: String,
        count: Int
    ): AppResult<List<ProductImageMaster>?, DataError.Remote> {
        val queryBody = buildFirestoreQuery(
            collection = DatabaseCollection.PRODUCT_IMAGES_MASTER,
            limit = count,
            filters = listOf(
                FirestoreFilter("productId", productId)
            )
        )

        val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
            httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }.body()
        }

        return when (response) {
            is AppResult.Error -> AppResult.Error(response.error)

            is AppResult.Success -> {
                val images: List<ProductImageMaster> = response.data.mapNotNull { result ->
                    val fields = result.document?.fields ?: return@mapNotNull null

                    ProductImageMaster(
                        productId = (fields["productId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        productImages = (fields["productImages"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                    )
                }

                AppResult.Success(images)
            }
        }
    }

    override suspend fun getActiveProductsWithImages(
        shopId: String,
    ): AppResult<List<ProductWithImages>, DataError.Remote> {
        return try {
            val filters = listOf(
                FirestoreFilter("shopId", shopId),
                FirestoreFilter("productActive", "0"),
                FirestoreFilter("flag", "0")
            )

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                filters = filters
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }
            }

            val queryResults = when (response) {
                is AppResult.Error -> return response
                is AppResult.Success -> response.data
            }

            val productList = mutableListOf<ProductWithImages>()

            coroutineScope {
                val jobs = queryResults.mapNotNull { result ->
                    val product = result.document?.toProductMaster() ?: return@mapNotNull null
                    async {
                        val imageResult = fetchProductImages(product.productId)
                        val images =
                            if (imageResult is AppResult.Success) imageResult.data else null
                        productList.add(ProductWithImages(product, images ?: emptyList()))
                    }
                }

                jobs.awaitAll()
            }

            AppResult.Success(productList)

        } catch (e: Exception) {
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.NOT_FOUND,
                    message = e.message ?: "Unknown error in getActiveProductsWithImages"
                )
            )
        }
    }

    override suspend fun getNearByProducts(
        userLat: Double,
        userLng: Double,
        radiusKm: Double
    ): Flow<AppResult<List<ShopItems>, DataError.Remote>> = channelFlow {
        try {
            println("🌍 User location: ($userLat, $userLng)")

            // Step 1: Compute user geohash and neighbors
            val userGeoHash = GeoUtils.encodeGeohash(userLat, userLng)
            val geoHashesToQuery = GeoUtils.getGeohashNeighbors(userGeoHash).take(10)
            println("📌 Geohashes to query: $geoHashesToQuery")

            // Step 2: Query Firestore for nearby active shops
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                filters = listOf(
                    FirestoreFilter("productActive", "0"),
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("shopActive", "0"),
                    FirestoreFilter("shopBlock", "0"),
                    FirestoreFilter("geoHash", geoHashesToQuery, FirestoreOperator.IN)
                ),
                limit = 200
            )

            val queryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (queryResponse is AppResult.Error) {
                println("❌ Failed to fetch shops.")
                send(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                return@channelFlow
            }

            if (queryResponse is AppResult.Success) {
                val validProduct = queryResponse.data.map { result ->
                    val fields = result.document?.fields ?: return@map null
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

                val productList = mutableListOf<ProductMaster>()

                for (product in validProduct) {
                    product?.let {
                        val lat = product.shopLatitude.toDoubleOrNull()
                        val lng = product.shopLongitude.toDoubleOrNull()
                        if (lat != null && lng != null) {
                            if (GeoUtils.isWithinRadiusKm(userLat, userLng, lat, lng, radiusKm)) {
                                productList += product
                            }
                        }
                    }
                }
                send(AppResult.Success(productList.toShopItemList()))
            }
        } catch (e: Exception) {
            println("❌ General error: ${e.message}")
            send(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
        }
    }
}


fun List<ProductMaster?>.toShopItemList(): List<ShopItems> {
    return this.mapNotNull { product ->
        product?.let {
            ShopItems(
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
                image = "",
            )
        }
    }
}