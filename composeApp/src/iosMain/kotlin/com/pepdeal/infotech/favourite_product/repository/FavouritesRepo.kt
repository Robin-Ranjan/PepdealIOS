package com.pepdeal.infotech.favourite_product.repository

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
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.favourite_product.modal.FavoriteProductMaster
import com.pepdeal.infotech.placeAPI.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class FavouritesRepo {
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun removeFavoriteItem(userId: String, productId: String, onDelete: () -> Unit) {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) { AppJson }
        }

        try {
            val requestBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("productId", productId)
                ),
                limit = 1
            )

            val response: HttpResponse = httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status != HttpStatusCode.OK) {
                println("Not Deleted")
            }


            val databaseResponse: List<DatabaseQueryResponse> = try {
                AppJson.decodeFromString(response.bodyAsText())
            } catch (e: Exception) {
                listOf(AppJson.decodeFromString<DatabaseQueryResponse>(response.bodyAsText()))
            }

            if (databaseResponse.isEmpty()) {
                println("No matching favorite doctor found for deletion.")
            }
            // Extract document ID
            val documentPath = databaseResponse.firstOrNull()?.document?.name
            if (documentPath.isNullOrEmpty()) {
                println("No document name found in response")
            }

            val documentId = documentPath?.substringAfterLast("/")
            println("Extracted Document ID for deletion: $documentId")

            val deleteResponse =
                httpClient.delete("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}/$documentId")

            if (deleteResponse.status == HttpStatusCode.OK) {
                println("Deleted Doctor Fav")
                onDelete()
            } else {
                println("Failed to delete favorite doctor. Status: ${deleteResponse.status}")
            }
        } catch (e: Exception) {
            println("❌ Error deleting favorite item: ${e.message}")
        } finally {
            client.close()
        }
    }


    suspend fun isFavorite(userId: String, productId: String): Boolean {
        return try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.FAVOURITE_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                    FirestoreFilter("productId", productId)
                ),
                limit = 1
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Success -> {
                    val found = response.data.any { it.document?.fields?.isNotEmpty() == true }
                    found
                }

                is AppResult.Error -> {
                    println("Firestore query error: ${response.error.message}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Exception in isFavorite: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    suspend fun addFavorite(product: FavoriteProductMaster) {
        try {
            val response =
                httpClient.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "userId" to DatabaseValue.StringValue(product.userId),
                                "productId" to DatabaseValue.StringValue(product.productId),
                                "createdAt" to DatabaseValue.StringValue(product.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(product.updatedAt)
                            )
                        )
                    )
                }

            if (response.status != HttpStatusCode.OK) {
                println("Error: ${response.status} ${response.bodyAsText()}")
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }
            val databaseResponse: DatabaseResponse = response.body()
            val generatedId = databaseResponse.name.substringAfterLast("/")

            val patchResponse =
                httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.FAVOURITE_MASTER}/$generatedId?updateMask.fieldPaths=favId") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "id" to DatabaseValue.StringValue(generatedId)
                            )
                        )
                    )
                }

            if (patchResponse.status == HttpStatusCode.OK) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
            }

        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
        }
    }

}