package com.pepdeal.infotech.shop

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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class EditShopDetailsRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin){
        install(ContentNegotiation){
            json
        }
    }

    suspend fun fetchShopServices(shopId: String): ShopStatusMaster = withContext(Dispatchers.IO) {
        return@withContext try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_status_master.json") {
                parameter("orderBy", "\"shopId\"")
                parameter("equalTo", "\"$shopId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val shopStatusMap: Map<String, ShopStatusMaster> = json.decodeFromString(response.bodyAsText())
                shopStatusMap.values.firstOrNull() ?: ShopStatusMaster()
            } else {
                ShopStatusMaster()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ShopStatusMaster()
        }
    }


    suspend fun updateShopServices(shopStatus: ShopStatusMaster){
        try {

        }catch (e:Exception){
            e.printStackTrace()
            println(e.message)
        }
    }

}