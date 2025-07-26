package com.pepdeal.infotech.shop.shopDetails

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseRequest
import com.pepdeal.infotech.core.databaseUtils.DatabaseResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.placeAPI.httpClient
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString

class OpenYourShopRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun registerShop(shopMaster: ShopMaster): Pair<Boolean, String> {
        return try {
            // Step 1: Check if shop already exists based on shopMobileNo
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                filters = listOf(FirestoreFilter("shopMobileNo", shopMaster.shopMobileNo)),
                limit = 1
            )

            val checkResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (checkResponse is AppResult.Error) {
                return false to "Firestore query failed: ${checkResponse.error}"
            }

            if (checkResponse is AppResult.Success) {
                val found = checkResponse.data.any { it.document?.fields?.isNotEmpty() == true }
                if (found) return false to "Shop Already Registered"
            }

            // Step 2: Add empty document to generate ID
            val createResponse =
                client.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SHOP_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "shopId" to DatabaseValue.StringValue(""),
                                "userId" to DatabaseValue.StringValue(shopMaster.userId ?: ""),
                                "shopName" to DatabaseValue.StringValue(shopMaster.shopName ?: ""),
                                "shopMobileNo" to DatabaseValue.StringValue(shopMaster.shopMobileNo),
                                "shopAddress" to DatabaseValue.StringValue(
                                    shopMaster.shopAddress ?: ""
                                ),
                                "shopAddress2" to DatabaseValue.StringValue(
                                    shopMaster.shopAddress2 ?: ""
                                ),
                                "shopArea" to DatabaseValue.StringValue(shopMaster.shopArea ?: ""),
                                "city" to DatabaseValue.StringValue(shopMaster.city ?: ""),
                                "state" to DatabaseValue.StringValue(shopMaster.state ?: ""),
                                "pinCode" to DatabaseValue.StringValue(shopMaster.pinCode),
                                "shopDescription" to DatabaseValue.StringValue(
                                    shopMaster.shopDescription ?: ""
                                ),
                                "bgColourId" to DatabaseValue.StringValue(
                                    shopMaster.bgColourId ?: ""
                                ),
                                "fontSizeId" to DatabaseValue.StringValue(
                                    shopMaster.fontSizeId ?: ""
                                ),
                                "fontStyleId" to DatabaseValue.StringValue(
                                    shopMaster.fontStyleId ?: ""
                                ),
                                "fontColourId" to DatabaseValue.StringValue(shopMaster.fontColourId),
                                "shopActive" to DatabaseValue.StringValue(
                                    shopMaster.shopActive ?: ""
                                ),
                                "flag" to DatabaseValue.StringValue(shopMaster.flag ?: ""),
                                "latitude" to DatabaseValue.StringValue(shopMaster.latitude ?: ""),
                                "longitude" to DatabaseValue.StringValue(
                                    shopMaster.longitude ?: ""
                                ),
                                "shopStatus" to DatabaseValue.StringValue(
                                    shopMaster.shopStatus ?: ""
                                ),
                                "searchTag" to DatabaseValue.ArrayValue(shopMaster.searchTag?.map {
                                    DatabaseValue.StringValue(
                                        it
                                    )
                                } ?: emptyList()),
                                "showNumber" to DatabaseValue.StringValue(shopMaster.showNumber),
                                "geoHash" to DatabaseValue.StringValue(shopMaster.geoHash ?: ""),
                                "isVerified" to DatabaseValue.StringValue(
                                    shopMaster.isVerified ?: ""
                                ),
                                "createdAt" to DatabaseValue.StringValue(
                                    shopMaster.createdAt ?: ""
                                ),
                                "updatedAt" to DatabaseValue.StringValue(shopMaster.updatedAt ?: "")

                            )
                        )
                    )
                }

            if (!createResponse.status.isSuccess()) {
                return false to "Failed to generate shopId"
            }

            val databaseResponse: DatabaseResponse = createResponse.body()
            val generatedId = databaseResponse.name.substringAfterLast("/")

            val patchResponse =
                httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SHOP_MASTER}/$generatedId?updateMask.fieldPaths=shopId") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "id" to DatabaseValue.StringValue(generatedId)
                            )
                        )
                    )
                }

            return if (patchResponse.status == HttpStatusCode.OK) {
                true to "Shop Registered"
            } else {
                false to "Registration Failed: ${patchResponse.status}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false to "Error checking Shop number availability."
        }
    }
}