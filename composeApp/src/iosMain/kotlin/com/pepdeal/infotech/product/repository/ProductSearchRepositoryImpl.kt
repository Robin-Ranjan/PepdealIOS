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
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ShopItems
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class ProductSearchRepositoryImpl(
    private val httpClient: HttpClient,
) : ProductSearchRepository {
    override fun getAllProductsSearchFlowPagination(
        userId: String?,
        startIndex: String?,
        pageSize: Int,
        searchQuery: String
    ): Flow<AppResult<List<ShopItems>, DataError.Remote>> = channelFlow {
        try {
            val query = searchQuery.trim().lowercase().split(" ").filter { it.isNotBlank() }
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                filters = listOf(
                    FirestoreFilter("productActive", "0"),
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("shopActive", "0"),
                    FirestoreFilter("shopBlock", "0"),
                    FirestoreFilter("searchTag", query, FirestoreOperator.ARRAY_CONTAINS)
                ),
                limit = 5000
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
//                            if (GeoUtils.isWithinRadiusKm(userLat, userLng, lat, lng, radiusKm)) {
                            productList += product
//                            }
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