package com.pepdeal.infotech.shop.shopDetails

import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
            // Step 1: Check if shop exists based on shopMobileNo
            val checkResponse: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                parameter("orderBy", "\"shopMobileNo\"")
                parameter("equalTo", "\"${shopMaster.shopMobileNo}\"")
                contentType(ContentType.Application.Json)
            }

            val checkBody = checkResponse.bodyAsText()
            println("Check Response: $checkBody") // Debug log

            if (checkBody.isNotEmpty() && checkBody != "{}") {
                return false to "Shop Already Registered"
            }

            // Step 2: Generate a Shop ID (equivalent to Firebase push().key)
            val keyResponse: HttpResponse = client.post("${FirebaseUtil.BASE_URL}shop_master.json") {
                contentType(ContentType.Application.Json)
                setBody("{}") // Firebase returns a unique key when we send an empty object
            }

            if (!keyResponse.status.isSuccess()) {
                return false to "Firebase request failed: ${keyResponse.status}"
            }

            val keyJson = keyResponse.body<Map<String,String>>()
            val shopId = keyJson["name"] ?: return false to "Error While Generating ShopId"

            // Step 3: Register the shop with the generated ID
            val shopWithId = shopMaster.copy(shopId = shopId)
            val registerResponse: HttpResponse = client.put("${FirebaseUtil.BASE_URL}shop_master/$shopId.json") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(shopWithId))
            }

            if (registerResponse.status == HttpStatusCode.OK) {
                true to "Shop Registered"
            } else {
                false to "Registration Failed"
            }
        } catch (e: Exception) {
            println("Error registering shop: ${e.message}")
            e.printStackTrace()
            false to "Error checking Shop number availability."
        }
    }

}