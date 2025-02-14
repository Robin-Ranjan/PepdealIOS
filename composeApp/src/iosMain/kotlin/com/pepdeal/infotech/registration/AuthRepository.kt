package com.pepdeal.infotech.registration

import UserMaster
import com.pepdeal.infotech.tickets.TicketMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.IO
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val client: HttpClient by lazy {
        try {
            HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        } catch (e: Exception) {
            println("HttpClient Initialization Error: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to initialize HttpClient", e)
        }
    }

    private const val FIREBASE_API_KEY = "AIzaSyAviy4SudRwo-r-YT1YyTL4wiWPKXzW1Wo"
    private const val BASE_URL = "https://identitytoolkit.googleapis.com/v1"

    @Serializable
    data class SendOTPRequest(val phoneNumber: String, val recaptchaToken: String = "dummy-token")

    @Serializable
    data class SendOTPResponse(val sessionInfo: String)

    suspend fun sendOtp(phoneNumber: String): String? {
        return withContext(Dispatchers.Default) {
            try {
                val formattedPhoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
                val requestBody = SendOTPRequest(phoneNumber = formattedPhoneNumber)

                println("Sending OTP Request: $requestBody")

                val response: HttpResponse = client.post("$BASE_URL/accounts:sendVerificationCode?key=$FIREBASE_API_KEY") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                println("$BASE_URL/accounts:sendVerificationCode?key=$FIREBASE_API_KEY")

                val responseText = response.bodyAsText() // Get error details
                println(responseText)

                if (response.status.isSuccess()) {
                    val otpResponse: SendOTPResponse = response.body()
                    println("OTP Session Info: ${otpResponse.sessionInfo}")
                    return@withContext otpResponse.sessionInfo
                } else {
                    println("Response Error: ${response.status}")
                    println("Error Details: $responseText") // Print Firebase error message
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Exception: ${e.message}")
                return@withContext null
            }
        }
    }

    suspend fun verifyOtp(otpCode: String, sessionInfo: String): String? {
        return withContext(Dispatchers.Default) {
            try {
                val requestBody = VerifyOTPRequest(code = otpCode, sessionInfo = sessionInfo)

                println("Verifying OTP: $requestBody")

                val response: HttpResponse = client.post("$BASE_URL/accounts:signInWithPhoneNumber?key=$FIREBASE_API_KEY") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val responseText = response.bodyAsText() // Get error details

                if (response.status.isSuccess()) {
                    val verifyResponse: VerifyOTPResponse = response.body()
                    println("Successfully Verified! ID Token: ${verifyResponse.idToken}")
                    return@withContext verifyResponse.idToken // This token is used for authentication
                } else {
                    println("Response Error: ${response.status}")
                    println("Error Details: $responseText") // Print Firebase error message
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Exception: ${e.message}")
                return@withContext null
            }
        }
    }

    @Serializable
    data class VerifyOTPRequest(
        val sessionInfo: String,
        val code: String
    )

    @Serializable
    data class VerifyOTPResponse(
        val idToken: String,  // Firebase ID Token (JWT)
        val refreshToken: String?,
        val expiresIn: String?
    )

    suspend fun checkUserAvailable(phoneNumber: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_master.json") {
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
                    return@withContext Pair(true, "User Found")  // ✅ Ensuring return inside `withContext`
                } else {
                    return@withContext Pair(false, "User Not Found")
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
                return@withContext Pair(false, "Error checking mobile number availability.")  // ✅ Ensure exception handling returns
            }
        }
    }

    suspend fun registerUser(userMaster: UserMaster): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO){
            try {

                // Get a unique Firebase-generated ID
                val keyResponse: HttpResponse = client.post("${FirebaseUtil.BASE_URL}user_master.json") {
                    contentType(ContentType.Application.Json)
                    setBody("{}") // Firebase returns a unique key when we send an empty object
                }
                println(keyResponse.bodyAsText())
                val keyJson = keyResponse.body<Map<String, String>>()
                val userId = keyJson["name"] ?: return@withContext Pair(false, "Error generating user ID.")

                println("userId:- $userId")
                val newUser = userMaster.copy(userId = userId, fcmToken = "")

                // Register User with the generated Firebase key
                val registerResponse: HttpResponse = client.put("${FirebaseUtil.BASE_URL}user_master/$userId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(newUser)
                }

                return@withContext if (registerResponse.status == HttpStatusCode.OK) {
                    Pair(true, "Registration Successful")
                } else {
                    Pair(false, "Registration Failed")
                }

            }catch (e:Exception){
                println(e.message)
                Pair(false, "Something Went Wrong")
            }
        }
    }

}
