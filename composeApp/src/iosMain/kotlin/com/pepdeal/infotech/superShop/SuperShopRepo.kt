package com.pepdeal.infotech.superShop

import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SuperShopRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin){
        install(ContentNegotiation){
            json
        }
    }

    fun getSuperShopWithProduct(userId:String): Flow<SuperShopsWithProduct?> = flow{
        try {
            val superShops = fetchSuperShops(userId)
                .sortedByDescending { it.createdAt.toLongOrNull()?: 0L }

            if(superShops.isEmpty()){
                emit(null)
            }

            for (superShop in superShops){
                val shopDetails = fetchShopDetails(superShop.shopId)
                if(shopDetails?.flag == "0" && shopDetails.isActive == "0"){
                    val products = getActiveProductsWithImages(superShop.shopId)
                    if(products.isNotEmpty()){
                        emit(SuperShopsWithProduct(shopDetails,products,superShop.createdAt))
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            emit(null)
        }
    }

    private suspend fun fetchSuperShops(userId: String):List<SuperShopMaster>{
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}super_shop_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val superShopMap: Map<String, SuperShopMaster> = json.decodeFromString(response.bodyAsText())
                superShopMap.values.toList()
            } else {
                emptyList()
            }

        }catch (e:Exception){
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchShopDetails(shopId: String): ShopMaster? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master/$shopId.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                json.decodeFromString(response.bodyAsText())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getActiveProductsWithImages(shopId: String): List<ProductWithImages> {
        val productList = mutableListOf<ProductWithImages>()
        println("product function")
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\"") {
//                parameter("orderBy", "\"shopId\"")
//                parameter("equalTo", "\"$shopId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
                println("product ${responseBody.length}")
                println("product response ${responseBody.toString()}")
                // Use coroutineScope to launch the product image fetching concurrently
                coroutineScope {
                    productsMap.values.forEach { product ->
                        if (product.isActive == "0" && product.flag == "0") {
                            async {
                                println("product ${product.productName}")
                                val images = getProductImages(product.productId)
                                val productWithImages = ProductWithImages(product, images)
                                productList.add(productWithImages)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error fetching product data: ${e.message}")
        }

        return productList
    }

    // Fetch images for a specific product
    private suspend fun getProductImages(productId: String): List<ProductImageMaster> = coroutineScope {
        val imageList = mutableListOf<ProductImageMaster>()

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
//                parameter("orderBy", "\"productId\"")
//                parameter("equalTo", "\"$productId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(responseBody)

                imageList.addAll(imagesMap.values)
            }
        } catch (e: Exception) {
            println("Error fetching product images: ${e.message}")
        }
        return@coroutineScope imageList
    }

    suspend fun removeSuperShop(userId: String, shopId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin)
        try {
            // 1️⃣ Query Firebase to get all items for the user
            val queryUrl = "${FirebaseUtil.BASE_URL}super_shop_master.json?orderBy=\"userId\"&equalTo=\"$userId\""
            val responseString: String = client.get(queryUrl).body()  // Fetch as String

            // 2️⃣ Parse JSON manually
            val responseJson = Json.parseToJsonElement(responseString).jsonObject

            // 3️⃣ Find the correct favId by filtering on productId
            val superShopId = responseJson.entries.firstOrNull { (_, jsonElement) ->
                jsonElement.jsonObject["shopId"]?.jsonPrimitive?.content == shopId
            }?.key

            if (superShopId != null) {
                // 4️⃣ Delete the specific item using favId
                val deleteUrl = "${FirebaseUtil.BASE_URL}super_shop_master/$superShopId.json"
                val deleteResponse: HttpResponse = client.delete(deleteUrl)

                if (deleteResponse.status == HttpStatusCode.OK) {
                    println("✅ Super shop deleted successfully.")
                    onDelete()
                } else {
                    println("❌ Failed to delete super shop item: ${deleteResponse.status}")
                }
            } else {
                println("⚠️ No matching super shop found for productId: $shopId")
            }
        } catch (e: Exception) {
            println("❌ Error deleting super shop: ${e.message}")
        } finally {
            client.close()
        }
    }
}