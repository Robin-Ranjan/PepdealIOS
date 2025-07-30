package com.pepdeal.infotech.product.producrDetails

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.shopDetails.ShopDetailsRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class ProductDetailsRepo {

    private val json = Json { ignoreUnknownKeys = true }
    val httpClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchTheProductDetails(
        productId: String,
        filterList: List<FirestoreFilter> = emptyList()
    ): ProductWithImages? {
        try {
            var productWIthImages =
                ProductWithImages(ProductMaster(), listOf(ProductImageMaster()))

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.PRODUCT_MASTER,
                limit = 1,
                filters = filterList.ifEmpty {
                    listOf(
                        FirestoreFilter("productId", productId),
                        FirestoreFilter("productActive", "0"),
                        FirestoreFilter("flag", "0"),
                        FirestoreFilter("shopActive", "0"),
                        FirestoreFilter("shopBlock", "0")
                    )
                }
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            val queryResults = when (response) {
                is AppResult.Error -> return null
                is AppResult.Success -> response.data
            }

            coroutineScope {
                val jobs = queryResults.mapNotNull { result ->
                    val fields = result.document?.fields ?: return@mapNotNull null
                    val product = ProductMaster(
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

                    async {
                        val images = ShopDetailsRepo().getProductImages(product.productId)
                        productWIthImages = ProductWithImages(product, images)
                    }
                }

                jobs.awaitAll()
            }
            return productWIthImages
        } catch (e: Exception) {
            println("Error fetching product details: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}