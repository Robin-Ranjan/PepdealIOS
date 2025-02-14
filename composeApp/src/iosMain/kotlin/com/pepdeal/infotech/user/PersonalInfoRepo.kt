package com.pepdeal.infotech.user

import UserMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PersonalInfoRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun updateUserEmailId(userId: String, updatedUserEmail: String): Boolean {
        return try {
            val httpClient = HttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val requestBody = mapOf(
                "emailId" to updatedUserEmail,
                "updatedAt" to Util.getCurrentTimeStamp()
            )

            val response: HttpResponse = httpClient.patch("${FirebaseUtil.BASE_URL}user_master/$userId.json") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun fetchUserDetails2(userId: String): UserMaster {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_master.json") {
                parameter("orderBy", "\"userId\"")
                parameter("equalTo", "\"$userId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val userMap: Map<String, UserMaster> = json.decodeFromString(response.bodyAsText())
                return userMap.values.firstOrNull() ?: UserMaster() // ✅ Return first matching user
            }
            UserMaster() // ✅ Return null when no data is found
        } catch (e: Exception) {
            e.printStackTrace()
            UserMaster()
        }
    }

}