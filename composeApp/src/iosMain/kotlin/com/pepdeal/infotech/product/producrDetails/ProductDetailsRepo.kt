package com.pepdeal.infotech.product.producrDetails

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class ProductDetailsRepo {

    private val json = Json { ignoreUnknownKeys = true }
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchTheProductDetails(productId: String): ProductWithImages? {
        try {
            println("Fetching details for product ID: $productId") // Debug Log

            // Fetch Product Details
            val productResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                parameter("orderBy", "\"productId\"")
                parameter("equalTo", "\"$productId\"")
                contentType(ContentType.Application.Json)
            }

            if (!productResponse.status.isSuccess()) {
                println("Failed to fetch product details. Status: ${productResponse.status}")
                return null
            }

            val responseBody = productResponse.bodyAsText()
            val jsonElement = Json.parseToJsonElement(responseBody).jsonObject

            if (jsonElement.isEmpty()) {
                println("No product found for ID: $productId")
                return null
            }

            val product = jsonElement.values.firstOrNull()?.let {
                Json.decodeFromJsonElement<ProductMaster>(it)
            } ?: return null

            println("Product found: ${product.productName}") // Debug Log

            // Fetch Product Images
            val imagesResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json") {
                parameter("orderBy", "\"productId\"")
                parameter("equalTo", "\"$productId\"")
                contentType(ContentType.Application.Json)
            }

            val images: List<ProductImageMaster> = if (imagesResponse.status.isSuccess()) {
                val imagesJson = imagesResponse.bodyAsText()
                val imagesMap: Map<String, ProductImageMaster> = Json.decodeFromString(imagesJson)
                imagesMap.values.toList()
            } else {
                println("No images found for product ID: $productId") // Debug Log
                emptyList()
            }

            println("Total images fetched: ${images.size}") // Debug Log

            return ProductWithImages(product, images)

        } catch (e: Exception) {
            println("Error fetching product details: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    suspend fun fetchTheProductShopDetails(shopId: String): ShopMaster? {
        try {
            println("Fetching details for Shop ID: $shopId") // Debug Log

            // Fetch Product Details
            val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                parameter("orderBy", "\"shopId\"")
                parameter("equalTo", "\"$shopId\"")
                contentType(ContentType.Application.Json)
            }

            if (!shopResponse.status.isSuccess()) {
                println("Failed to fetch shop details. Status: ${shopResponse.status}")
                return null
            }

            val responseBody = shopResponse.bodyAsText()
            val jsonElement = Json.parseToJsonElement(responseBody).jsonObject

            if (jsonElement.isEmpty()) {
                println("No Shop found for ID: $shopResponse")
                return null
            }

            val shop = jsonElement.values.firstOrNull()?.let {
                Json.decodeFromJsonElement<ShopMaster>(it)
            } ?: return null

            println("Shop found: ${shop.shopName}") // Debug Log
            return shop

        } catch (e: Exception) {
            println("Error fetching shop details: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

}