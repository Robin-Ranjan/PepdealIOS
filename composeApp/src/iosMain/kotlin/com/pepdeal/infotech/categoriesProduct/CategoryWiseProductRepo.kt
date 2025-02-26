package com.pepdeal.infotech.categoriesProduct

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ShopItems
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
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
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

    fun getTheCategoryWiseProduct(subCategoryName: String): Flow<List<ShopItems>> = flow {
        val nameFormatSubCategory = subCategoryName.toNameFormat().trim()
        try {
            println("Fetching products for subCategory: $nameFormatSubCategory") // Debug Log

            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                parameter("orderBy", "\"subCategoryId\"")
                parameter("equalTo", "\"$nameFormatSubCategory\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                val jsonElement = Json.parseToJsonElement(responseText).jsonObject

                println("Total products fetched: ${jsonElement.size}") // Debug Log

                if (jsonElement.isEmpty()) {
                    println("No products found for subCategory: $nameFormatSubCategory") // Debug Log
                    emit(emptyList())
                    return@flow
                }

                for ((_, productJson) in jsonElement) {
                    val product = Json.decodeFromJsonElement<ProductMaster>(productJson)

                    if (product.isActive == "0" && product.flag == "0" && product.subCategoryId.toNameFormat()
                            .trim() == nameFormatSubCategory
                    ) {
                        println("Processing product: ${product.productName} (ID: ${product.productId})") // Debug Log

                        val imageUrl = fetchProductImage(product.productId)
                        val shopItem = product.toShopItem(imageUrl)

                        emit(listOf(shopItem))
                    } else {
                        println("Skipping product: ${product.productName} $subCategoryName ${product.subCategoryId} ${product.categoryId}") // Debug Log
                    }
                }
            } else {
                println("Failed to fetch products: ${response.status}") // Debug Log
                emit(emptyList())
            }
        } catch (e: Exception) {
            println("Error fetching products: ${e.message}") // Debug Log
            e.printStackTrace()
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO) // Run on IO dispatcher for better performance

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
                println("Image found: $imageUrl") // Debug Log
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