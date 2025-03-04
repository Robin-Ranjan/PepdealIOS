package com.pepdeal.infotech.product


import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ProductRepo {
    private val json = Json{ignoreUnknownKeys =true}

    fun getAllProductsFlowPagination(startIndex: String?, pageSize: Int): Flow<ShopItems> = channelFlow {
        val client = HttpClient(Darwin){
            install(ContentNegotiation){
                json(Json{ignoreUnknownKeys = true})
            }
        }

        try {
            // **✅ Pagination: Use `startAfter` Instead of `startAt`**
            val url = if (startIndex != null) {
                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&startAfter=\"$startIndex\"&limitToFirst=$pageSize"
            } else {
                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&limitToFirst=$pageSize"
            }

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)

                // **✅ Filter Active Products**
                val validProducts = productsMap.values.filter { it.isActive == "0" && it.flag == "0" }

                if (validProducts.isEmpty()) {
                    println("No more products available")
                    return@channelFlow
                }

                // **✅ Use `async` for Concurrent API Calls**
                coroutineScope {
                    validProducts.map { product ->
                        async {
                            try {
                                // **✅ Fetch Shop Details**
                                val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                                    contentType(ContentType.Application.Json)
                                }

                                val shop: ShopMaster? = if (shopResponse.status == HttpStatusCode.OK) {
                                    val shopBody: String = shopResponse.bodyAsText()
                                    val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                                    shopMap.values.firstOrNull()
                                } else null

                                // **✅ Fetch Product Image**
                                val imageResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                    contentType(ContentType.Application.Json)
                                }

                                val imageUrl: String? = if (imageResponse.status == HttpStatusCode.OK) {
                                    val imageBody: String = imageResponse.bodyAsText()
                                    val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)
                                    imagesMap.values.firstOrNull()?.productImages
                                } else null

                                // **✅ Only Emit If Shop is Active**
                                if (shop != null && shop.isActive == "0" && shop.flag == "0") {
                                    val shopItem = ShopItems(
                                        image = imageUrl.orEmpty(),
                                        productId = product.productId,
                                        shopId = product.shopId,
                                        productName = product.productName,
                                        sellingPrice = product.sellingPrice,
                                        mrp = product.mrp,
                                        description = product.description,
                                        category = product.categoryId,
                                        discountMrp = product.discountMrp,
                                        isActive = product.isActive,
                                        flag = product.flag,
                                        subCategoryId = product.subCategoryId,
                                        searchTag = product.searchTag,
                                        onCall = product.onCall
                                    )

                                    send(shopItem)
                                    println("✅ Emitted product ${product.productId}")
                                }
                            } catch (e: Exception) {
                                println("⚠️ Error fetching details for product ${product.productId}: ${e.message}")
                            }
                        }
                    }.awaitAll() // **✅ Wait for all async calls to finish**
                }

                // **✅ Update Pagination Index**
//                val newStartIndex = validProducts.lastOrNull()?.productId
//                if (validProducts.size < pageSize) {
//                    endReached = true
//                }
            } else {
                throw Exception("Error fetching product data: ${response.status}")
            }
        } catch (e: Exception) {
            println("⚠️ Error: ${e.message}")
        }
        finally {
            client.close()
        }
    }

    fun getAllProductsSearchFlowPagination(startIndex: String?, pageSize: Int,searchQuery: String = ""): Flow<ShopItems> = channelFlow {
        val client = HttpClient(Darwin){
            install(ContentNegotiation){
                json(Json{ignoreUnknownKeys = true})
            }
        }

        try {
            // **✅ Pagination: Use `startAfter` Instead of `startAt`**
//            val url = if (startIndex != null) {
//                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&startAfter=\"$startIndex\"&limitToFirst=$pageSize"
//            } else {
//                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&limitToFirst=$pageSize"
//            }

            val url =  "${FirebaseUtil.BASE_URL}product_master.json"

            val response: HttpResponse = client.get(url) {
                parameter("orderBy","\"isActive\"")
                parameter("equalTo", "\"0\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)

                // **✅ Filter Active Products**
//                val validProducts = productsMap.values.filter { it.isActive == "0" && it.flag == "0" }
                var validProducts = productsMap.values

                println("valid product 1 : ${validProducts.size}")
                if (validProducts.isEmpty()) {
                    println("No more products available")
                    return@channelFlow
                }else {
                    validProducts = validProducts
                        .filter { product ->
                            product.searchTag
                                .split(",")
                                .any { tag -> tag.trim().contains(searchQuery, ignoreCase = true) } ?: false
                        }
                        .sortedByDescending { product ->
                            product.searchTag.split(",")
                                .map { tag ->
                                    val trimmed = tag.trim()
                                    when {
                                        trimmed.equals(searchQuery, ignoreCase = true) -> 3  // Exact match
                                        trimmed.startsWith(searchQuery, ignoreCase = true) -> 2 // Starts with query
                                        trimmed.contains(searchQuery, ignoreCase = true) -> 1  // Contains query
                                        else -> 0
                                    }
                                }.sum()
                        }
                }

                println("valid product : ${validProducts.size.toString()}")
                validProducts.forEach { product ->
                    launch {
                        try {
                            // **✅ Fetch Shop Details**
                            val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                                contentType(ContentType.Application.Json)
                            }

                            val shop: ShopMaster? = if (shopResponse.status == HttpStatusCode.OK) {
                                val shopBody: String = shopResponse.bodyAsText()
                                val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                                shopMap.values.firstOrNull()
                            } else null

                            // **✅ Fetch Product Image**
                            val imageResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                contentType(ContentType.Application.Json)
                            }

                            val imageUrl: String? = if (imageResponse.status == HttpStatusCode.OK) {
                                val imageBody: String = imageResponse.bodyAsText()
                                val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)
                                imagesMap.values.firstOrNull()?.productImages
                            } else null

                            // **✅ Only Emit If Shop is Active**
                            if (shop != null && shop.isActive == "0" && shop.flag == "0") {
                                val shopItem = ShopItems(
                                    image = imageUrl.orEmpty(),
                                    productId = product.productId,
                                    shopId = product.shopId,
                                    productName = product.productName,
                                    sellingPrice = product.sellingPrice,
                                    mrp = product.mrp,
                                    description = product.description,
                                    category = product.categoryId,
                                    discountMrp = product.discountMrp,
                                    isActive = product.isActive,
                                    flag = product.flag,
                                    subCategoryId = product.subCategoryId,
                                    searchTag = product.searchTag,
                                    onCall = product.onCall
                                )

                                println("✅ searched Emitted product ${product.productId}")
                                send(shopItem)
                            }
                        } catch (e: Exception) {
                            println("⚠️ Error fetching details for product ${product.productId}: ${e.message}")
                        }
                    }
                }
            } else {
                throw Exception("Error fetching product data: ${response.status}")
            }
        } catch (e: Exception) {
            println("⚠️ Error: ${e.message}")
        }
//        finally {
//            client.close()
//        }
    }

}

object HttpClientProvider {
    val client = HttpClient(Darwin){
        install(ContentEncoding){

        }
    }
}