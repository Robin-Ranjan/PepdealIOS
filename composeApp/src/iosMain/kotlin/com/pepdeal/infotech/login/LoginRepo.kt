package com.pepdeal.infotech.login

import UserMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class LoginRepo {
    // Initialize Ktor Client
    private val client = HttpClient(Darwin)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun validateUserLogin(
        mobileNo: String,
        pass: String,
        onResult: (Boolean, String) -> Unit
    ) {
         val client = HttpClient(Darwin)
        try {
//            println("${FirebaseUtil.BASE_URL}user_master.json?orderBy=\"mobileNo\"&equalTo=\"$mobileNo\"")
//            val response: HttpResponse =
//                client.get("${FirebaseUtil.BASE_URL}user_master.json?orderBy=\"mobileNo\"&equalTo=\"+919113381241\"") {
////                client.get("${FirebaseUtil.BASE_URL}user_master.json?orderBy=\"mobileNo\"&equalTo=\"+919113381241\"") {
//                    contentType(ContentType.Application.Json)
//                }
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_master.json") {
                parameter("orderBy", "\"mobileNo\"")
                parameter("equalTo", "\"$mobileNo\"")
                contentType(ContentType.Application.Json)
            }

            println("login response ${response.status}")
            if (response.status == HttpStatusCode.OK) {

                val responseBody: String = response.bodyAsText()
                if (responseBody == "null" || responseBody.isBlank()) {
                    println("User Not Found")
                    onResult(false, "User Not Found")
                    return
                }
                val userMap: Map<String, UserMaster> = json.decodeFromString(responseBody)
                println(userMap.values)
                if (userMap.isEmpty()) {
                    println("User Not Found Map")
                    onResult(false, "User Not Found")
                } else {
                    coroutineScope {
                        userMap.values.forEach { userMaster ->
                            if (userMaster.password == pass) {
                                println("Login Successfully")
                                onResult(true, "Login Successfully")
                            } else {
                                println("Incorrect Password")
                                onResult(false, "Incorrect Password")
                            }
                        }
                    }
                }
            } else {
                onResult(false, "User Not Found")
                println("User Not Found")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        } finally {
            client.close()
        }
    }
}