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
import com.pepdeal.infotech.shop.modal.ShopMaster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class SearchShopRepositoryImpl(
    private val httpClient: HttpClient,
) : SearchShopRepository {

    override suspend fun getActiveSearchedShopsFlowPagination(
        lastShopId: String?,
        pageSize: Int,
        searchQuery: String
    ): Flow<List<ShopMaster>> = channelFlow {
        try {
            val query = searchQuery.trim().lowercase().split(" ").filter { it.isNotBlank() }
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                filters = listOf(
                    FirestoreFilter("shopActive", "0"),
                    FirestoreFilter("flag", "0"),
                    FirestoreFilter("searchTag", query, FirestoreOperator.ARRAY_CONTAINS)
                ),
                limit = 5000
            )

            println("query body : $queryBody")
            val queryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (queryResponse is AppResult.Error) {
                println("❌ Firestore query failed: ${queryResponse.error.message}")
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
            send(validShops)

            println("🎯 Completed nearby shop scan.")
        } catch (e: Exception) {
            println("❌ Exception in getNearbyActiveShopsFlow: ${e.message}")
        }
    }
}



