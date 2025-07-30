package com.pepdeal.infotech.shop.repository

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
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.core.utils.GeoUtils
import com.pepdeal.infotech.product.repository.ProductRepository
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopWithProducts
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

class ShopRepositoryImpl(
    private val httpClient: HttpClient
) : ShopRepository {
    override suspend fun fetchShopDetails(shopId: String): AppResult<ShopMaster, DataError.Remote> {
        val queryBody = buildFirestoreQuery(
            collection = DatabaseCollection.SHOP_MASTER,
            limit = 1,
            filters = listOf(
                FirestoreFilter("shopId", shopId),
                FirestoreFilter("shopActive", "0"),
                FirestoreFilter("flag", "0")
            )
        )

        val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
            httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }.body()
        }

        return when (response) {
            is AppResult.Error -> AppResult.Error(response.error)

            is AppResult.Success -> {
                val doc = response.data.firstOrNull()?.document
                if (doc?.fields == null) {
                    // 🔴 No document found — return Fail
                    AppResult.Error(
                        DataError.Remote(
                            DataError.RemoteType.NOT_FOUND,
                            "Shop not found for ID: $shopId"
                        )
                    )
                } else {
                    val fields = doc.fields
                    val shop = ShopMaster(
                        shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopName = (fields["shopName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopMobileNo = (fields["shopMobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopAddress = (fields["shopAddress"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopAddress2 = (fields["shopAddress2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopArea = (fields["shopArea"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        state = (fields["state"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        pinCode = (fields["pinCode"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopDescription = (fields["shopDescription"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bgColourId = (fields["bgColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontSizeId = (fields["fontSizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontStyleId = (fields["fontStyleId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontColourId = (fields["fontColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        latitude = (fields["latitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        longitude = (fields["longitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopStatus = (fields["shopStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values
                            ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                            .orEmpty(),
                        isVerified = (fields["isVerified"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        showNumber = (fields["showNumber"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        geoHash = (fields["geoHash"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                    )

                    AppResult.Success(shop)
                }
            }
        }
    }

    override suspend fun getShopMobile(shopId: String): String? {
        return try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                filters = listOf(
                    FirestoreFilter("shopId", shopId)
                ),
                limit = 1
            )

            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }

            val responseBody = response.bodyAsText()
            val parsed: List<DatabaseQueryResponse> = AppJson.decodeFromString(responseBody)

            val firstMatch = parsed.firstOrNull { it.document != null } ?: return null
            val fields = firstMatch.document?.fields ?: return null

            (fields["shopMobileNo"] as? DatabaseValue.StringValue)?.stringValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getNearbyActiveShopsFlow(
        userLat: Double,
        userLng: Double,
        radiusKm: Double
    ): Flow<AppResult<ShopMaster, DataError.Remote>> = channelFlow {
        try {
            println("🌍 User location: ($userLat, $userLng)")

            val userGeoHash = GeoUtils.encodeGeohash(userLat, userLng)
            val geoHashesToQuery = GeoUtils.getGeohashNeighbors(userGeoHash).take(10)
            println("📌 Geohashes to query: $geoHashesToQuery")

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                filters = listOf(
                    FirestoreFilter("shopActive", "0"),
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("geoHash", geoHashesToQuery, FirestoreOperator.IN)
                ),
                limit = 200
            )

            val queryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (queryResponse is AppResult.Error) {
                println("❌ Firestore query failed: ${queryResponse.error.message}")
                send(AppResult.Error(DataError.Remote(type = DataError.RemoteType.EMPTY_RESULT)))
                return@channelFlow
            }

            val validShops = (queryResponse as AppResult.Success).data.mapNotNull { result ->
                val fields = result.document?.fields ?: return@mapNotNull null
                try {
                    ShopMaster(
                        shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopName = (fields["shopName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopMobileNo = (fields["shopMobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopAddress = (fields["shopAddress"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopAddress2 = (fields["shopAddress2"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopArea = (fields["shopArea"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        state = (fields["state"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        pinCode = (fields["pinCode"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopDescription = (fields["shopDescription"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bgColourId = (fields["bgColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontSizeId = (fields["fontSizeId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontStyleId = (fields["fontStyleId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fontColourId = (fields["fontColourId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopActive = (fields["shopActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        flag = (fields["flag"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        latitude = (fields["latitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        longitude = (fields["longitude"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        shopStatus = (fields["shopStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values
                            ?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                            .orEmpty(),
                        isVerified = (fields["isVerified"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        showNumber = (fields["showNumber"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        geoHash = (fields["geoHash"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                    )
                } catch (e: Exception) {
                    println("❌ Failed to parse shop document: ${e.message}")
                    null
                }
            }

            for (shop in validShops) {
                val lat = shop.latitude?.toDoubleOrNull()
                val lng = shop.longitude?.toDoubleOrNull()

                if (lat == null || lng == null) {
                    println("⚠️ Invalid lat/lng for shopId=${shop.shopId}")
                    continue
                }

                if (!GeoUtils.isWithinRadiusKm(userLat, userLng, lat, lng, radiusKm)) {
                    println("📏 Skipping shop outside radius: ${shop.shopName}")
                    continue
                }

//                val shopId = shop.shopId
//                if (!shopId.isNullOrBlank()) {
//                    val productsResult = productRepo.value.getActiveProductsWithImages(shopId)
//                    if (productsResult is AppResult.Success && productsResult.data.isNotEmpty()) {
//                        println("✅ Emitting: ${shop.shopName} with ${productsResult.data.size} products")
//                        send(AppResult.Success(ShopWithProducts(shop, productsResult.data)))
//                    } else {
//                        println("⚠️ No valid products for shop $shopId")
//                    }
//                } else {
//                    println("❌ Skipping shop due to null or blank shopId")
//                }

                send(AppResult.Success(shop))

            }

            println("🎯 Completed nearby shop scan.")
        } catch (e: Exception) {
            println("❌ Exception in getNearbyActiveShopsFlow: ${e.message}")
            send(
                AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.EMPTY_RESULT,
                        message = e.message
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}