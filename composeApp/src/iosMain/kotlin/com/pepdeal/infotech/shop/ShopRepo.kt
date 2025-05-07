package com.pepdeal.infotech.shop


import com.pepdeal.infotech.banner.BannerMaster
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import com.pepdeal.infotech.util.FirebaseUtil
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.math.*


class ShopRepo {

    val json = Json { ignoreUnknownKeys = true }

    private suspend fun getActiveProductsWithImages(shopId: String): List<ProductWithImages> {
//        val client = HttpClient(Darwin)
        val productList = mutableListOf<ProductWithImages>()
//        println("product function")
        try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\"") {
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val productsMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
//                println("product ${responseBody.length}")
//                println("product response ${responseBody.toString()}")
                // Use coroutineScope to launch the product image fetching concurrently
                coroutineScope {
                    productsMap.values.forEach { product ->
                        if (product.isActive == "0" && product.flag == "0") {
                            async {
//                                println("product ${product.productName}")
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
    private suspend fun getProductImages(productId: String): List<ProductImageMaster> =
        coroutineScope {
//        val client = HttpClient(Darwin)
            val imageList = mutableListOf<ProductImageMaster>()

            try {
                val response: HttpResponse =
                    client.get("${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
                        contentType(ContentType.Application.Json)
                    }

                if (response.status == HttpStatusCode.OK) {
                    val responseBody: String = response.bodyAsText()

                    val imagesMap: Map<String, ProductImageMaster> =
                        json.decodeFromString(responseBody)

                    imageList.addAll(imagesMap.values)
                }
            } catch (e: Exception) {
                println("Error fetching product images: ${e.message}")
            }
            return@coroutineScope imageList
        }

//    fun getActiveShopsFlowPaginationEmitWithFilter(
//        lastShopId: String? = null,
//        pageSize: Int = 20
//    ): Flow<ShopWithProducts> = channelFlow { // ✅ Use channelFlow for concurrency
//        try {
//            val url = if (lastShopId != null) {
//                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&startAt=\"$lastShopId\"&limitToFirst=$pageSize"
//            } else {
//                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&limitToFirst=$pageSize"
//            }
//
//            val response: HttpResponse = client.get(url) {
//                contentType(ContentType.Application.Json)
//            }
//
//            if (response.status == HttpStatusCode.OK) {
//                val responseBody: String = response.bodyAsText()
//                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)
//
//                val activeShops = shopsMap.values.filter { it.flag == "0" }
//
//                activeShops.forEach { shop ->
//                    launch { // ✅ Launch a coroutine for each shop (without blocking)
//                        val products = withContext(Dispatchers.IO) {
//                            getActiveProductsWithImages(shop.shopId ?: "-1")
//                        }
//                        if (products.isNotEmpty()) {
//                            println("emitting:- ${shop.shopName}")
//                            send(ShopWithProducts(shop, products))
//                        }
//                    }
//                }
//            } else {
//                throw Exception("Error fetching shop data: ${response.status}")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun getNearbyActiveShopsFlow(
        userLat: Double = 28.7162092,
        userLng: Double = 77.1170743,
        radiusKm: Double = 10.0
    ): Flow<ShopWithProducts> = channelFlow {
        try {
            val url = "${FirebaseUtil.BASE_URL}shop_master.json"

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)

                // Filter + cache distance + sort by nearest
                val nearbyShops = shopsMap.values.mapNotNull { shop ->
                    if (shop.flag == "0" && shop.latitude != null && shop.longitude != null) {
                        val lat = shop.latitude.toDoubleOrNull()
                        val lon = shop.longitude.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val distance = calculateDistanceInKm(userLat, userLng, lat, lon)
                            if (distance <= radiusKm) {
                                shop to distance
                            } else null
                        } else null
                    } else null
                }.sortedBy { it.second } // Sort by distance

                nearbyShops.forEach { (shop, distance) ->
                    launch {
                        val products = async(Dispatchers.IO) {
                            getActiveProductsWithImages(shop.shopId ?: "-1")
                        }.await()

                        if (products.isNotEmpty()) {
                            println("✅ emitting nearby shop: ${shop.shopName} at $distance km")
                            send(ShopWithProducts(shop, products))
                        }
                    }
                }
            } else {
                throw Exception("❌ Error fetching shop data: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateDistanceInKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // km
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()

        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun Double.toRadians(): Double = this * PI / 180

    // Fetch images for a specific product
    suspend fun getActiveBannerImages(): List<BannerMaster> = coroutineScope {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json
            }
        }
        val imageList = mutableListOf<BannerMaster>()

        try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}banner_master.json?orderBy=\"isActive\"&equalTo=\"0\"") {
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()

                val imagesMap: Map<String, BannerMaster> = json.decodeFromString(responseBody)

                imageList.addAll(imagesMap.values)
            }
        } catch (e: Exception) {
            println("Error fetching banner images: ${e.message}")
        } finally {
            client.close()
        }
        return@coroutineScope imageList
    }

    private val client = HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }


    fun getActiveSearchedShopsFlowPagination(
        lastShopId: String? = null,
        pageSize: Int = 200,
        searchQuery: String = ""
    ): Flow<ShopWithProducts> = channelFlow {
        try {
//            val url = if (lastShopId != null) {
//                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&startAt=\"$lastShopId\"&limitToFirst=$pageSize"
//            } else {
//                "${FirebaseUtil.BASE_URL}shop_master.json?orderBy=\"shopId\"&limitToFirst=$pageSize"
//            }
            val url = "${FirebaseUtil.BASE_URL}shop_master.json"

            val response: HttpResponse = client.get(url) {
                parameter("orderBy", "\"isActive\"")
                parameter("equalTo", "\"0\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody: String = response.bodyAsText()
                val shopsMap: Map<String, ShopMaster> = json.decodeFromString(responseBody)
//                var activeShops = shopsMap.values.filter { it.flag == "0" }
                var activeShops = shopsMap.values

                // If a search query is provided, filter activeShops using searchTag (case-insensitive)
                if (searchQuery.isNotEmpty()) {
                    activeShops = activeShops
                        .filter { shop ->
                            shop.searchTag
                                ?.split(",")
                                ?.any { tag -> tag.trim().contains(searchQuery, ignoreCase = true) }
                                ?: false
                        }
                        .sortedByDescending { shop ->
                            shop.searchTag?.split(",")
                                ?.map { tag ->
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
                                }?.sum() ?: 0
                        }
                }

                // For each active (and filtered) shop, concurrently fetch products and send result
                activeShops.forEach { shop ->
                    launch {
                        val products = withContext(Dispatchers.IO) {
                            getActiveProductsWithImages(shop.shopId ?: "-1")
                        }
                        if (products.isNotEmpty()) {
                            println("search emitting :- ${shop.shopName}")
                            send(ShopWithProducts(shop, products))
                        }
                    }
                }
            } else {
                throw Exception("Error fetching shop data: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}