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
import kotlin.math.*

class ProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun getAllProductsFlowPagination(startIndex: String?, pageSize: Int): Flow<ShopItems> =
        channelFlow {
            val client = HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
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
                    val productsMap: Map<String, ProductMaster> =
                        json.decodeFromString(responseBody)

                    // **✅ Filter Active Products**
                    val validProducts =
                        productsMap.values.filter { it.isActive == "0" && it.flag == "0" }

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
                                    val shopResponse: HttpResponse =
                                        client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                                            contentType(ContentType.Application.Json)
                                        }

                                    val shop: ShopMaster? =
                                        if (shopResponse.status == HttpStatusCode.OK) {
                                            val shopBody: String = shopResponse.bodyAsText()
                                            val shopMap: Map<String, ShopMaster> =
                                                json.decodeFromString(shopBody)
                                            shopMap.values.firstOrNull()
                                        } else null

                                    // **✅ Fetch Product Image**
                                    val imageResponse: HttpResponse =
                                        client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                            contentType(ContentType.Application.Json)
                                        }

                                    val imageUrl: String? =
                                        if (imageResponse.status == HttpStatusCode.OK) {
                                            val imageBody: String = imageResponse.bodyAsText()
                                            val imagesMap: Map<String, ProductImageMaster> =
                                                json.decodeFromString(imageBody)
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
                                            onCall = product.onCall,
                                            isShopBlock = product.isShopBlock,
                                            isShopActive = product.isShopActive,
                                            shopLongitude = product.shopLongitude,
                                            shopLatitude = product.shopLatitude
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
                } else {
                    throw Exception("Error fetching product data: ${response.status}")
                }
            } catch (e: Exception) {
                println("⚠️ Error: ${e.message}")
            } finally {
                client.close()
            }
        }

    fun getAllProductsSearchFlowPagination(
        startIndex: String?,
        pageSize: Int,
        searchQuery: String = ""
    ): Flow<ShopItems?> = channelFlow {


        try {
            // **✅ Pagination: Use `startAfter` Instead of `startAt`**
//            val url = if (startIndex != null) {
//                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&startAfter=\"$startIndex\"&limitToFirst=$pageSize"
//            } else {
//                "${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&limitToFirst=$pageSize"
//            }

            val url = "${FirebaseUtil.BASE_URL}product_master.json"

            val response: HttpResponse = client.get(url) {
                parameter("orderBy", "\"isActive\"")
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
                    send(null)
                    return@channelFlow
                } else {
                    validProducts = validProducts
                        .filter { product ->
                            product.searchTag
                                .split(",")
                                .any { tag -> tag.trim().contains(searchQuery, ignoreCase = true) }
                                ?: false
                        }
                        .sortedByDescending { product ->
                            product.searchTag.split(",")
                                .map { tag ->
                                    val trimmed = tag.trim()
                                    when {
                                        trimmed.equals(
                                            searchQuery,
                                            ignoreCase = true
                                        ) -> 3  // Exact match
                                        trimmed.startsWith(
                                            searchQuery,
                                            ignoreCase = true
                                        ) -> 2 // Starts with query
                                        trimmed.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        ) -> 1  // Contains query
                                        else -> 0
                                    }
                                }.sum()
                        }
                }


                if (validProducts.isEmpty()) {
                    send(null)
                    return@channelFlow
                }
                validProducts.forEach { product ->
                    launch {
                        try {
                            // **✅ Fetch Shop Details**
                            val shopResponse: HttpResponse =
                                client.get("${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&equalTo=\"${product.shopId}\"") {
                                    contentType(ContentType.Application.Json)
                                }

                            val shop: ShopMaster? = if (shopResponse.status == HttpStatusCode.OK) {
                                val shopBody: String = shopResponse.bodyAsText()
                                val shopMap: Map<String, ShopMaster> =
                                    json.decodeFromString(shopBody)
                                shopMap.values.firstOrNull()
                            } else null

                            // **✅ Fetch Product Image**
                            val imageResponse: HttpResponse =
                                client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                    contentType(ContentType.Application.Json)
                                }

                            val imageUrl: String? = if (imageResponse.status == HttpStatusCode.OK) {
                                val imageBody: String = imageResponse.bodyAsText()
                                val imagesMap: Map<String, ProductImageMaster> =
                                    json.decodeFromString(imageBody)
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
            send(null)
            println("⚠️ Error: ${e.message}")
        }
//        finally {
//            client.close()
//        }
    }

    fun getAllNearbyProductsFlow(
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 10.0
    ): Flow<ShopItems> = channelFlow {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            println("🌐 Fetching all products from: ${FirebaseUtil.BASE_URL}product_master.json")

            val url = "${FirebaseUtil.BASE_URL}product_master.json"
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            println("📥 Response status: ${response.status}")

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                println("🧾 Product response length: ${responseBody.length}")

                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
                println("🔍 Total products fetched: ${productsMap.size}")

                val validProductsWithDistance = productsMap.values.mapNotNull { product ->
                    try {
                        if (product.isActive == "0" && product.flag == "0" &&
                            product.isShopActive == "0" && product.isShopBlock == "0"
                        ) {
                            val lat = product.shopLatitude.toDoubleOrNull()
                            val lon = product.shopLongitude.toDoubleOrNull()

                            if (lat != null && lon != null) {
                                val distance = calculateDistanceInKm(userLat, userLng, lat, lon)
                                println("📍 Product ${product.productId} is $distance km away")

                                if (distance <= radiusKm) {
                                    product to distance
                                } else {
                                    println("❌ Skipping ${product.productId}: outside radius")
                                    null
                                }
                            } else {
                                println("⚠️ Skipping ${product.productId}: invalid coordinates")
                                null
                            }
                        } else {
                            println("❌ Skipping ${product.productId}: inactive or blocked")
                            null
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error processing product ${product.productId}: ${e.message}")
                        null
                    }
                }.sortedBy { it.second }

                println("✅ Valid nearby products count: ${validProductsWithDistance.size}")

                coroutineScope {
                    validProductsWithDistance.map { (product, distance) ->
                        async {
                            try {
                                println("📸 Fetching image for ${product.productId}")

                                val imageResponse: HttpResponse =
                                    client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"${product.productId}\"") {
                                        contentType(ContentType.Application.Json)
                                    }

                                val imageUrl: String? = if (imageResponse.status == HttpStatusCode.OK) {
                                    val imageBody: String = imageResponse.bodyAsText()
                                    val imagesMap: Map<String, ProductImageMaster> =
                                        json.decodeFromString(imageBody)
                                    imagesMap.values.firstOrNull()?.productImages
                                } else {
                                    println("⚠️ No image found for ${product.productId}")
                                    null
                                }

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
                                    isShopBlock = product.isShopBlock,
                                    isShopActive = product.isShopActive,
                                    shopLongitude = product.shopLongitude,
                                    shopLatitude = product.shopLatitude
                                )

                                send(shopItem)
                                println("✅ Emitted product ${product.productId} at $distance km")
                            } catch (e: Exception) {
                                println("⚠️ Error emitting product ${product.productId}: ${e.message}")
                            }
                        }
                    }.awaitAll()
                }
            } else {
                throw Exception("❌ Error fetching product data: ${response.status}")
            }
        } catch (e: Exception) {
            println("❌ General error: ${e.message}")
        } finally {
            println("🔒 Closing HTTP client")
            client.close()
        }
    }

    private fun calculateDistanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun Double.toRadians(): Double = this * PI / 180
}

object HttpClientProvider {
    val client = HttpClient(Darwin) {
        install(ContentEncoding) {

        }
    }
}