package com.pepdeal.infotech.categoriesProduct

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ShopItems
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.Util.toNameFormat
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class CategoryWiseProductRepo() {
    private val json = Json { ignoreUnknownKeys = true }
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    fun getTheCategoryWiseProduct(subCategoryName: String): Flow<ShopItems?> = channelFlow {
        val nameFormatSubCategory = subCategoryName.toNameFormat().trim()
        println("Fetching products for subCategory: $nameFormatSubCategory")

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                parameter("orderBy", "\"subCategoryId\"")
                parameter("equalTo", "\"$nameFormatSubCategory\"")
                contentType(ContentType.Application.Json)
            }

            if (!response.status.isSuccess()) {
                println("Failed to fetch products: ${response.status}")
                send(null)
                return@channelFlow
            }

            val responseText = response.bodyAsText()
            val jsonElement = Json.parseToJsonElement(responseText).jsonObject

            if (jsonElement.isEmpty()) {
                println("No products found for subCategory: $nameFormatSubCategory")
                send(null)
                return@channelFlow
            }
            coroutineScope {
                jsonElement.values.map { productJson ->
                    val product = Json.decodeFromJsonElement<ProductMaster>(productJson)

                    async {
                        val shopResponse =
                            client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                                contentType(ContentType.Application.Json)
                            }

                        val shop: ShopMaster? = if (shopResponse.status.isSuccess()) {
                            val shopBody = shopResponse.bodyAsText()
                            val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                            shopMap.values.firstOrNull()
                        } else null

                        if (shop == null || shop.isActive != "0" || shop.flag != "0") return@async null

                        if (product.isActive == "0" && product.flag == "0" &&
                            product.subCategoryId.toNameFormat().trim() == nameFormatSubCategory
                        ) {
//                            println("Processing product: ${product.productName} (ID: ${product.productId})")

                            val imageUrl = fetchProductImage(product.productId)
                            send(product.toShopItem(imageUrl))
                        } else {
//                            println("Skipping product: ${product.productName}")
                            null
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            println("Error fetching products: ${e.message}")
            e.printStackTrace()
            send(null)
        }
    }.flowOn(Dispatchers.IO) // Run on


    /**
     * Fetches the first product image from Firebase.
     */
    private suspend fun fetchProductImage(productId: String): String? {
        return try {
            println("Fetching image for product ID: $productId") // Debug Log

            val imageResponse: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_images_master.json") {
                    parameter("orderBy", "\"productId\"")
                    parameter("equalTo", "\"$productId\"")
                    contentType(ContentType.Application.Json)
                }

            if (imageResponse.status.isSuccess()) {
                val imageBody = imageResponse.bodyAsText()
                val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)

                val imageUrl = imagesMap.values.firstOrNull()?.productImages
//                println("Image found: $imageUrl") // Debug Log
                imageUrl
            } else {
                println("No image found for product ID: $productId, Response: ${imageResponse.status}") // Debug Log
                null
            }
        } catch (e: Exception) {
            println("Error fetching image: ${e.message}") // Debug Log
            e.printStackTrace()
            null
        }
    }

    /**
     * Extension function to convert `ProductMaster` to `ShopItems`.
     */
    private fun ProductMaster.toShopItem(imageUrl: String?): ShopItems {
        return ShopItems(
            image = imageUrl.orEmpty(),
            productId = this.productId,
            shopId = this.shopId,
            productName = this.productName,
            sellingPrice = this.sellingPrice,
            mrp = this.mrp,
            description = this.description,
            category = this.categoryId,
            discountMrp = this.discountMrp,
            isActive = this.isActive,
            flag = this.flag,
            subCategoryId = this.subCategoryId,
            searchTag = this.searchTag,
            onCall = this.onCall,
        )
    }
}