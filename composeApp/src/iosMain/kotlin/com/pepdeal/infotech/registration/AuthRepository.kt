package com.pepdeal.infotech.registration

import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.OtpAuthKeys
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object AuthRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val client: HttpClient by lazy {
        try {
            HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(json)
                }
            }
        } catch (e: Exception) {
            println("HttpClient Initialization Error: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to initialize HttpClient", e)
        }
    }


    suspend fun sendOtp(
        phoneNumber: String,
        isForgotPassword: Boolean = false,
        isResend: Boolean = false
    ): Boolean {
        val formattedPhoneNumber = phoneNumber.replace(Regex("^\\+"), "")
        val baseUrl = "https://control.msg91.com/api/v5/otp"
        val url = if (isResend) "$baseUrl/retry" else baseUrl

        val templateId = if (isForgotPassword) OtpAuthKeys.FORGET_PASS_OTP_TEMPLATE_ID
        else OtpAuthKeys.REGISTER_OTP_TEMPLATE_ID
        return try {
            val response: HttpResponse = client.get(url) {
                url {
                    parameters.append("mobile", formattedPhoneNumber)
                    parameters.append("authkey", OtpAuthKeys.AUTH_KEY)
                    if (!isResend) {
                        parameters.append("template_id", templateId)
                        parameters.append("otp_length", "6")
                        parameters.append("otp_expiry", "10")
                    } else {
                        parameters.append("retrytype", "text")
                    }
                }
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            val responseBody = response.bodyAsText()
            println("OTP Response: $responseBody")

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error sending OTP: ${e.message}")
            false
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String): Boolean {
        val formattedPhoneNumber = phoneNumber.replace(Regex("^\\+"), "")
        val url =
            "https://control.msg91.com/api/v5/otp/verify?mobile=$formattedPhoneNumber&otp=$otp&authkey=${OtpAuthKeys.AUTH_KEY}"

        return try {
            val response: HttpResponse = client.get(url)
            response.status.value == 200
        } catch (e: Exception) {
            println("Error verifying OTP: ${e.message}")
            false
        }
    }

    suspend fun checkUserAvailable(phoneNumber: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse =
                    client.get("${FirebaseUtil.BASE_URL}user_master.json") {
                        parameter("orderBy", "\"mobileNo\"")
                        parameter("equalTo", "\"$phoneNumber\"")
                        contentType(ContentType.Application.Json)
                    }

                if (response.status == HttpStatusCode.OK) {
                    val responseBody: String = response.bodyAsText()
                    println(responseBody)
                    if (responseBody == "null" || responseBody.isBlank() || responseBody == "{}") {
                        println("User Not Found")
                        return@withContext Pair(false, "User Not Found")  // ✅ Corrected return
                    }
                    return@withContext Pair(
                        true,
                        "User Found"
                    )  // ✅ Ensuring return inside `withContext`
                } else {
                    return@withContext Pair(false, "User Not Found")
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
                return@withContext Pair(
                    false,
                    "Error checking mobile number availability."
                )  // ✅ Ensure exception handling returns
            }
        }
    }

    suspend fun registerUser(userMaster: UserMaster): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                // Get a unique Firebase-generated ID
                val keyResponse: HttpResponse =
                    client.post("${FirebaseUtil.BASE_URL}user_master.json") {
                        contentType(ContentType.Application.Json)
                        setBody("{}") // Firebase returns a unique key when we send an empty object
                    }

                val keyJson = keyResponse.body<Map<String, String>>()
                val userId =
                    keyJson["name"] ?: return@withContext Pair(false, "Error generating user ID.")

                val newUser = userMaster.copy(userId = userId, fcmToken = "")

                // Register User with the generated Firebase key
                val registerResponse: HttpResponse =
                    client.put("${FirebaseUtil.BASE_URL}user_master/$userId.json") {
                        contentType(ContentType.Application.Json)
                        setBody(newUser)
                    }

                if (registerResponse.status != HttpStatusCode.OK) {
                    return@withContext Pair(false, "Registration Failed")
                }

                println("User mobile No:- ${userMaster.mobileNo}")

                // Check if the user is also a shop owner
                val shopResponse: HttpResponse =
                    client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                        parameter("orderBy", "\"shopMobileNo\"")
                        parameter("equalTo", "\"${userMaster.mobileNo}\"")
                        contentType(ContentType.Application.Json)
                    }
                val shopJson = shopResponse.body<Map<String, ShopMaster>>()
                println(shopJson)

                if (shopJson.isNotEmpty()) {
                    val shopEntry = shopJson.entries.first()
                    val shopId = shopEntry.key

                    println("Updating shop with shopId: $shopId")

                    val shopUpdates = mapOf("userId" to userId)

                    val shopUpdateResponse: HttpResponse =
                        client.patch("${FirebaseUtil.BASE_URL}shop_master/$shopId.json") {
                            contentType(ContentType.Application.Json)
                            setBody(shopUpdates)
                        }

                    if (shopUpdateResponse.status != HttpStatusCode.OK) {
                        return@withContext Pair(
                            false,
                            "User registered but shop userId update failed."
                        )
                    }
                } else {
                    println("empty json")
                }

                val userUpdates = mapOf("userStatus" to "1")
                val updateUserStatusResponse: HttpResponse =
                    client.patch("${FirebaseUtil.BASE_URL}user_master/$userId.json") { // Update specific user node
                        contentType(ContentType.Application.Json)
                        setBody(userUpdates)
                    }


                if (updateUserStatusResponse.status != HttpStatusCode.OK) {
                    return@withContext Pair(false, "User registered but userStatus update failed.")
                }

                Pair(true, "Registration Successful")
            } catch (e: Exception) {
                println(e.message)
                Pair(false, "Something Went Wrong")
            }
        }
    }

    suspend fun updateUserPassword(mobileNo: String, updatedPass: String): Boolean {
        return try {
            val client = HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(json)
                }
            }
            // Step 1: Fetch user ID by matching mobile number
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_master.json") {
                parameter("orderBy", "\"mobileNo\"")
                parameter("equalTo", "\"$mobileNo\"")
                contentType(ContentType.Application.Json)
            }

            val responseBody: String = response.bodyAsText()
            val userMap: Map<String, UserMaster>? = json.decodeFromString(responseBody)

            println(userMap)
            val userId = userMap?.keys?.firstOrNull() ?: return false // Extract the userId

            println(userId)

            val requestBody = mapOf(
                "password" to updatedPass,
                "updatedAt" to Util.getCurrentTimeStamp()
            )

            val updateResponse: HttpResponse =
                client.patch("${FirebaseUtil.BASE_URL}user_master/$userId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            updateResponse.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            false
        }
    }
}
