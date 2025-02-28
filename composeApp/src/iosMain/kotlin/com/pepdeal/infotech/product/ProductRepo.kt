package com.pepdeal.infotech.product


import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ProductRepo {
    private val json = Json{ignoreUnknownKeys =true}


    private suspend fun fetchActiveProducts(client: HttpClient): List<ShopItems> {
        val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"isActive\"&equalTo=\"0\"") {
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            json.decodeFromString(response.body())
        } else {
            emptyList()
        }
    }

    private suspend fun fetchShopDetails(client: HttpClient, shopId: String): Boolean {
        val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\""){
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            val shopData = json.decodeFromString<Map<String, String>>(response.body())
            shopData["flag"] == "0" && shopData["isActive"] == "0"
        } else {
            false
        }
    }

    private suspend fun fetchProductImage(client: HttpClient, productId: String): String? {
        val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
//            parameter("productId", productId)
//            parameter("limit", 1)
            contentType(ContentType.Application.Json)
        }
        return if (response.status == HttpStatusCode.OK) {
            val images = Json.decodeFromString<List<Map<String, String>>>(response.body())
            images.firstOrNull()?.get("productImages")
        } else {
            null
        }
    }


    fun getAllProductsFlow(): Flow<ShopItems> = callbackFlow {
        val client = HttpClient(Darwin)
        var count =0

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                // Deserialize JSON into Map
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)

                // Reverse the data to have the last item first
                val reversedProducts = productsMap.values.toList().reversed()

                coroutineScope {
                    val deferredProducts = productsMap.map { (_, product) ->
//                        println("product:- ${product.productName}")
                        async {
                            if (product.isActive == "0" && product.flag == "0") {
                                val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\""){
                                    contentType(ContentType.Application.Json)
                                }

                                if (shopResponse.status == HttpStatusCode.OK) {
                                    val shopBody: String = shopResponse.bodyAsText()
                                    val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                                    val shop: ShopMaster? = shopMap.values.firstOrNull()
//                                    println( "product shop :- ${shop?.shopName}")

                                    if (shop != null && shop.isActive == "0" && shop.flag == "0") {
                                        val imageResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
//                                            parameter("orderBy", "\"productId\"")
//                                            parameter("equalTo", "\"${product.productId}\"")
//                                            parameter("limitToFirst", 1)
                                            contentType(ContentType.Application.Json)
                                        }

                                        if (imageResponse.status == HttpStatusCode.OK) {
                                            val imageBody: String = imageResponse.bodyAsText()
                                            val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)
                                            val imageUrl = imagesMap.values.firstOrNull()?.productImages

                                            // Assign image URL to product
//                                            product.image = imageUrl.orEmpty()
                                            val shopItem = ShopItems(
                                                image = imageUrl.orEmpty(),
                                                productId = product.productId,
                                                shopId = product.shopId,
                                                productName= product.productName,
                                                sellingPrice= product.sellingPrice,
                                                mrp= product.mrp,
                                                description= product.description,
                                                category= product.categoryId,
                                                discountMrp= product.discountMrp,
                                                isActive= product.isActive,
                                                flag= product.flag,
                                                subCategoryId= product.subCategoryId,
                                                searchTag= product.searchTag,
                                                onCall= product.onCall,
                                            )
                                            return@async shopItem
                                        }
                                    }
                                }
                            }
                            null
                        }
                    }

                    // Wait for all requests and emit valid products
//                    deferredProducts.awaitAll().filterNotNull().forEach { shopItem ->
//                        println(count++)
//                        trySend(shopItem)
//                    }

                    val validProducts = deferredProducts.awaitAll().filterNotNull()
                    println("Total valid products: ${validProducts.size}")  // Log the number of valid products

                    // Emit products one by one
                    validProducts.forEach { product ->
                        trySend(product) // Emitting products one by one
                        println("Emitting product: ${product.productId} ${count++}") // Additional log for emitted product
                    }
                }
            } else {
                close(Exception("Error fetching product data: ${response.status}"))
            }
        } catch (e: Exception) {
            close(e)
        } finally {
            client.close()
        }

        awaitClose { }
    }

    suspend fun getAllProductsList(): List<ShopItems> {
        val client = HttpClient(Darwin)
        var count = 0
        val shopItems = mutableListOf<ShopItems>() // A mutable list to store the products

        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                // Deserialize JSON into Map
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)

                // Reverse the data to have the last item first
                val reversedProducts = productsMap.values.toList().reversed()

                coroutineScope {
                    val deferredProducts = productsMap.map { (_, product) ->
                        async {
                            if (product.isActive == "0" && product.flag == "0") {
                                val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\""){
                                    contentType(ContentType.Application.Json)
                                }

                                if (shopResponse.status == HttpStatusCode.OK) {
                                    val shopBody: String = shopResponse.bodyAsText()
                                    val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                                    val shop: ShopMaster? = shopMap.values.firstOrNull()

                                    if (shop != null && shop.isActive == "0" && shop.flag == "0") {
                                        val imageResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                            contentType(ContentType.Application.Json)
                                        }

                                        if (imageResponse.status == HttpStatusCode.OK) {
                                            val imageBody: String = imageResponse.bodyAsText()
                                            val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)
                                            val imageUrl = imagesMap.values.firstOrNull()?.productImages

                                            // Create a ShopItems object and add to the list
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
                                                onCall = product.onCall,
                                            )
                                            shopItems.add(shopItem)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Wait for all requests and collect valid products
                    deferredProducts.awaitAll()
                }

                // Return the list of all valid products
                println("Total valid products: ${shopItems.size}")
                return shopItems

            } else {
                throw Exception("Error fetching product data: ${response.status}")
            }
        } catch (e: Exception) {
            throw e // Propagate the exception
        } finally {
            client.close()
        }
    }


    fun getAllProductsFlowPagination(startIndex: Int, pageSize: Int): Flow<ShopItems> = flow {
        val client = HttpClientProvider.client
        var count = 0
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)

                // Reverse data to get the latest products first
//                val allProducts = productsMap.values.toList().reversed()
                val allProducts = productsMap.values

                // Apply pagination
                val paginatedProducts = allProducts.drop(startIndex).take(pageSize)

                for (product in paginatedProducts) {
                    if (product.isActive == "0" && product.flag == "0") {
                        val shopResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                            contentType(ContentType.Application.Json)
                        }

                        if (shopResponse.status == HttpStatusCode.OK) {
                            val shopBody: String = shopResponse.bodyAsText()
                            val shopMap: Map<String, ShopMaster> = json.decodeFromString(shopBody)
                            val shop: ShopMaster? = shopMap.values.firstOrNull()

                            if (shop != null && shop.isActive == "0" && shop.flag == "0") {
                                val imageResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                    contentType(ContentType.Application.Json)
                                }

                                if (imageResponse.status == HttpStatusCode.OK) {
                                    val imageBody: String = imageResponse.bodyAsText()
                                    val imagesMap: Map<String, ProductImageMaster> = json.decodeFromString(imageBody)
                                    val imageUrl = imagesMap.values.firstOrNull()?.productImages

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
                                        onCall = product.onCall,
                                    )

                                    emit(shopItem) // ✅ Emit each item **one by one**
                                    println("emitting ${count++}")
                                }
                            }
                        }
                    }
                }
            } else {
                throw Exception("Error fetching product data in pagination: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        } finally {
            client.close()
        }
    }

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
        } finally {
            client.close()
        }
    }

}

object HttpClientProvider {
    val client = HttpClient(Darwin){
        install(ContentEncoding){

        }
    }
}