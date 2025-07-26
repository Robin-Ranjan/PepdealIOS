package com.pepdeal.infotech.banner

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }
private val client = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}


// Fetch images for a specific product
suspend fun getActiveBannerImages(): List<BannerMaster> = try {

    val queryBody = buildFirestoreQuery(
        collection = DatabaseCollection.BANNER_MASTER,
        filters = listOf(
            FirestoreFilter("isActive", "0")
        )
    )

    val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
        client.post(DatabaseUtil.DATABASE_QUERY_URL) {
            contentType(ContentType.Application.Json)
            setBody(queryBody)
        }
    }

    when (response) {
        is AppResult.Success -> {
            response.data.mapNotNull { doc ->
                doc.document?.fields?.let { fields ->
                    BannerMaster(
                        bannerId = (fields["bannerId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bannerImage = (fields["bannerImage"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bannerName = (fields["bannerName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bannerDescription = (fields["bannerDescription"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        bannerOrder = (fields["bannerOrder"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                    )
                }
            }
        }

        is AppResult.Error -> {
            println("🔥 Firestore error: ${response.error.message}")
            emptyList()
        }
    }
} catch (e: Exception) {
    println("🔥 Exception fetching banners: ${e.message}")
    emptyList()
}