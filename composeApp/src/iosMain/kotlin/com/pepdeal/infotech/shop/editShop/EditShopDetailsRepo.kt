package com.pepdeal.infotech.shop.editShop

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseRequest
import com.pepdeal.infotech.core.databaseUtils.DatabaseResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestorePatchUrl
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shop.modal.ShopStatusMaster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class EditShopDetailsRepo {
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }

    suspend fun fetchShopDetails(shopId: String): ShopMaster? {
        return try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                limit = 1,
                filters = listOf(
                    FirestoreFilter("shopId", shopId),
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }
            when (response) {
                is AppResult.Error -> null

                is AppResult.Success -> {
                    val shop = response.data.firstOrNull()?.document?.fields?.let { fields ->
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
                            searchTag = (fields["searchTag"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            isVerified = (fields["isVerified"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            showNumber = (fields["showNumber"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            geoHash = (fields["geoHash"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()

                        )
                    }
                    return shop
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchShopServices(shopId: String): ShopStatusMaster = withContext(Dispatchers.IO) {
        return@withContext try {
            println("📡 Building Firestore query for shopId = $shopId")
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_STATUS_MASTER,
                limit = 1,
                filters = listOf(FirestoreFilter("shopId", shopId))
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Success -> {
                    val result = response.data.firstOrNull()
                    if (result == null) {
                        println("⚠️ No shop status found for shopId = $shopId")
                        ShopStatusMaster()
                    } else {
                        println("✅ Shop status document found for shopId = $shopId")
                        result.document?.fields?.let { fields ->
                            ShopStatusMaster(
                                shopStatusId = (fields["shopStatusId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                shopId = (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                cashOnDelivery = (fields["cashOnDelivery"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                doorStep = (fields["doorStep"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                homeDelivery = (fields["homeDelivery"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                liveDemo = (fields["liveDemo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                offers = (fields["offers"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                bargain = (fields["bargain"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                                updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                            )
                        } ?: ShopStatusMaster().also {
                            println("⚠️ Shop status fields are null for shopId = $shopId")
                        }
                    }
                }
                is AppResult.Error -> {
                    println("❌ Firestore query error for shopId = $shopId: ${response.error.message}")
                    ShopStatusMaster()
                }
            }
        } catch (e: Exception) {
            println("❌ Exception fetching shop services for shopId = $shopId: ${e.message}")
            e.printStackTrace()
            ShopStatusMaster()
        }
    }

    suspend fun updateShopDetails(shopMaster: ShopMaster): Boolean {
        return try {
            // Step 1: Build query to fetch the shop by `shopId` and `userId`
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                filters = listOf(
                    FirestoreFilter("shopId", shopMaster.shopId ?: "-1"),
                    FirestoreFilter("userId", shopMaster.userId ?: "-1")
                ),
                limit = 1
            )

            // Step 2: Execute query
            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (response is AppResult.Success && response.data.isNotEmpty()) {
                val documentName = response.data.first().document?.name ?: return false
                val documentId = documentName.substringAfterLast("/")

                // Step 3: Prepare updates
                val updateFields = mapOf(
                    "shopDescription" to DatabaseValue.StringValue(
                        shopMaster.shopDescription ?: ""
                    ),
                    "bgColourId" to DatabaseValue.StringValue(shopMaster.bgColourId ?: ""),
                    "fontStyleId" to DatabaseValue.StringValue(shopMaster.fontStyleId ?: ""),
                    "fontColourId" to DatabaseValue.StringValue(shopMaster.fontColourId ?: ""),
                    "showNumber" to DatabaseValue.StringValue(shopMaster.showNumber),
                    "updatedAt" to DatabaseValue.StringValue(shopMaster.updatedAt ?: "")
                )

                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.SHOP_MASTER,
                    documentId = documentId,
                    fields = updateFields.keys.toList()
                )

                // Step 4: PATCH to Firestore
                val patchResponse =
                    client.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(DatabaseRequest(fields = updateFields))
                    }

                return patchResponse.status.isSuccess()
            }

            false
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error updating shop details: ${e.message}")
            false
        }
    }


    suspend fun insertOrUpdateShopServices(shopStatus: ShopStatusMaster): Boolean {
        return try {
            println("🔍 Step 1: Building Firestore query to check if entry exists for shopId=${shopStatus.shopId} and userId=${shopStatus.userId}")

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_STATUS_MASTER,
                filters = listOf(
                    FirestoreFilter("shopId", shopStatus.shopId),
                    FirestoreFilter("userId", shopStatus.userId)
                ),
                limit = 1
            )

            println("📡 Step 2: Sending Firestore query...")
            val checkResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (checkResponse is AppResult.Success && checkResponse.data.isNotEmpty()) {
                println("✅ Entry found in Firestore, proceeding to update...")
                println(checkResponse)

                val documentRef = checkResponse.data.first().document
                val documentId = documentRef?.name?.substringAfterLast("/") ?: run {
                    println("⚠️ Document or document name is null. Cannot proceed with update.")
                    return false
                }

                println("📄 Existing document ID: $documentId")

                val updates = mapOf(
                    "cashOnDelivery" to DatabaseValue.StringValue(shopStatus.cashOnDelivery),
                    "doorStep" to DatabaseValue.StringValue(shopStatus.doorStep),
                    "homeDelivery" to DatabaseValue.StringValue(shopStatus.homeDelivery),
                    "liveDemo" to DatabaseValue.StringValue(shopStatus.liveDemo),
                    "offers" to DatabaseValue.StringValue(shopStatus.offers),
                    "bargain" to DatabaseValue.StringValue(shopStatus.bargain),
                    "updatedAt" to DatabaseValue.StringValue(shopStatus.updatedAt)
                )

                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.SHOP_STATUS_MASTER,
                    documentId = documentId,
                    fields = updates.keys.toList()
                )
                println("🔧 PATCH URL: $patchUrl")

                val patchResponse: HttpResponse = client.patch(patchUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(DatabaseRequest(fields = updates))
                }

                println("📬 PATCH Response Status: ${patchResponse.status}")
                return patchResponse.status.isSuccess()
            }

            println("➕ No existing entry found. Creating new document...")

            val createResponse: HttpResponse = client.post(
                "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SHOP_STATUS_MASTER}"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "shopStatusId" to DatabaseValue.StringValue(""),
                            "shopId" to DatabaseValue.StringValue(shopStatus.shopId),
                            "userId" to DatabaseValue.StringValue(shopStatus.userId),
                            "cashOnDelivery" to DatabaseValue.StringValue(shopStatus.cashOnDelivery),
                            "doorStep" to DatabaseValue.StringValue(shopStatus.doorStep),
                            "homeDelivery" to DatabaseValue.StringValue(shopStatus.homeDelivery),
                            "liveDemo" to DatabaseValue.StringValue(shopStatus.liveDemo),
                            "offers" to DatabaseValue.StringValue(shopStatus.offers),
                            "bargain" to DatabaseValue.StringValue(shopStatus.bargain),
                            "createdAt" to DatabaseValue.StringValue(shopStatus.createdAt),
                            "updatedAt" to DatabaseValue.StringValue(shopStatus.updatedAt)
                        )
                    )
                )
            }

            println("📬 CREATE Response Status: ${createResponse.status}")
            if (!createResponse.status.isSuccess()) {
                println("❌ Failed to create new document")
                return false
            }

            val createBody: DatabaseResponse = createResponse.body()
            val generatedId = createBody.name.substringAfterLast("/")
            println("🆔 Generated document ID: $generatedId")

            println("📝 Updating shopStatusId field...")
            val patchResponse: HttpResponse = client.patch(
                "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SHOP_STATUS_MASTER}/$generatedId?updateMask.fieldPaths=shopStatusId"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf("shopStatusId" to DatabaseValue.StringValue(generatedId))
                    )
                )
            }

            println("📬 Final PATCH Response Status: ${patchResponse.status}")
            return patchResponse.status.isSuccess()
        } catch (e: Exception) {
            println("🚨 Exception occurred in insertOrUpdateShopServices: ${e.message}")
            e.printStackTrace()
            false
        }
    }

}