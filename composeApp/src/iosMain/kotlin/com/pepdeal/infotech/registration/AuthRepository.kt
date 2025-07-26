package com.pepdeal.infotech.registration

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
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.OtpAuthKeys
import com.pepdeal.infotech.util.Util
import com.pepdeal.infotech.util.Util.toNameFormat
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
        userName: String = "",
        isForgotPassword: Boolean = false,
        isResend: Boolean = false
    ): Boolean {
        val formattedPhoneNumber = phoneNumber.replace(Regex("^\\+"), "")
        val otpBaseUrl = "https://control.msg91.com/api/v5/otp"
        val url = if (isResend) "$otpBaseUrl/retry" else otpBaseUrl

        val templateId = if (isForgotPassword) OtpAuthKeys.FORGET_PASS_OTP_TEMPLATE_ID
        else OtpAuthKeys.REGISTER_CUSTOMISED_OTP_TEMPLATE_ID

        val jsonObject = buildJsonObject {
            put("mobile", formattedPhoneNumber)
            put("authkey", OtpAuthKeys.AUTH_KEY)
            if (!isForgotPassword) {
                put("name", userName.toNameFormat()) // ✅ Correctly sending name
            }
            if (!isResend) {
                put("template_id", templateId)
                put("otp_length", OtpAuthKeys.OTP_LENGTH)
                put("otp_expiry", "10")
            } else {
                put("retrytype", "text")
            }
        }

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(jsonObject)
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
                println("🔍 Checking Firestore for user with phoneNumber=$phoneNumber")

                // Step 1: Build Firestore query body
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.USER_MASTER,
                    filters = listOf(
                        FirestoreFilter("mobileNo", phoneNumber)
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

                // Step 3: Check result
                return@withContext when {
                    response is AppResult.Success && response.data.isNotEmpty() && response.data.first().document != null -> {
                        println("✅ User Found in Firestore")
                        true to "User Found"
                    }

                    response is AppResult.Success -> {
                        println("❌ User Not Found (Empty or null document)")
                        false to "User Not Found"
                    }

                    else -> {
                        println("❌ Firestore query failed")
                        false to "Failed to check user"
                    }
                }
            } catch (e: Exception) {
                println("🚨 Exception in checkUserAvailable (Firestore): ${e.message}")
                false to "Error checking user in Firestore"
            }
        }
    }

    suspend fun registerUser(userMaster: UserMaster): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Create user in Firestore
                val createResponse: HttpResponse = client.post(
                    "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USER_MASTER}"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody(DatabaseRequest(fields = mapOf())) // empty body to get Firestore doc ID
                }

                if (!createResponse.status.isSuccess()) {
                    return@withContext Pair(false, "Error generating user ID.")
                }

                val createBody: DatabaseResponse = createResponse.body()
                val userId = createBody.name.substringAfterLast("/")
                val newUser = userMaster.copy(userId = userId, fcmToken = "")

                val updateFields = mapOf(
                    "userId" to DatabaseValue.StringValue(newUser.userId),
                    "userName" to DatabaseValue.StringValue(newUser.userName),
                    "mobileNo" to DatabaseValue.StringValue(newUser.mobileNo),
                    "emailId" to DatabaseValue.StringValue(newUser.emailId),
                    "password" to DatabaseValue.StringValue(newUser.password),
                    "fcmToken" to DatabaseValue.StringValue(newUser.fcmToken),
                    "deviceToken" to DatabaseValue.StringValue(newUser.deviceToken),
                    "isActive" to DatabaseValue.StringValue(newUser.isActive),
                    "createdAt" to DatabaseValue.StringValue(newUser.createdAt),
                    "updatedAt" to DatabaseValue.StringValue(newUser.updatedAt)
                )


                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.USER_MASTER,
                    documentId = userId,
                    fields = updateFields.keys.toList()
                )
                // Step 2: Set user data
                val patchUserResponse =
                    client.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(fields = updateFields)
                        )
                    }

                if (!patchUserResponse.status.isSuccess()) {
                    return@withContext Pair(false, "Registration Failed")
                }

                // Step 3: Search for existing shop with this mobile number
                val shopQueryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.SHOP_MASTER,
                    filters = listOf(FirestoreFilter("shopMobileNo", userMaster.mobileNo)),
                    limit = 1
                )

                val shopResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> =
                    safeCall {
                        client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                            contentType(ContentType.Application.Json)
                            setBody(shopQueryBody)
                        }.body()
                    }

                if (shopResponse is AppResult.Success && shopResponse.data.isNotEmpty()) {
                    val shopDoc = shopResponse.data.first().document?.name ?: ""
                    val shopId = shopDoc.substringAfterLast("/")

                    val shopUpdates = mapOf("userId" to DatabaseValue.StringValue(userId))
                    val patchShopResponse =
                        client.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.SHOP_MASTER}/$shopId?updateMask.fieldPaths=userId") {
                            contentType(ContentType.Application.Json)
                            setBody(DatabaseRequest(fields = shopUpdates))
                        }

                    if (!patchShopResponse.status.isSuccess()) {
                        return@withContext Pair(
                            false,
                            "User registered but shop userId update failed."
                        )
                    }
                }

                // Step 4: Update userStatus = "1"
                val userStatusPatch = mapOf("userStatus" to DatabaseValue.StringValue("1"))
                val userStatusResponse =
                    client.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USER_MASTER}/$userId?updateMask.fieldPaths=userStatus") {
                        contentType(ContentType.Application.Json)
                        setBody(DatabaseRequest(fields = userStatusPatch))
                    }

                if (!userStatusResponse.status.isSuccess()) {
                    return@withContext Pair(false, "User registered but userStatus update failed.")
                }

                Pair(true, "Registration Successful")
            } catch (e: Exception) {
                println("🔥 Exception in registerUser: ${e.message}")
                e.printStackTrace()
                Pair(false, "Something Went Wrong")
            }
        }
    }


    suspend fun updateUserPassword(mobileNo: String, updatedPass: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Build Firestore query to find user by mobile number
                val queryBody = buildFirestoreQuery(
                    collection = DatabaseCollection.USER_MASTER,
                    filters = listOf(FirestoreFilter("mobileNo", mobileNo)),
                    limit = 1
                )

                val queryResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                    client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                        contentType(ContentType.Application.Json)
                        setBody(queryBody)
                    }.body()
                }

                if (queryResponse !is AppResult.Success || queryResponse.data.isEmpty()) {
                    println("❌ User not found for mobileNo=$mobileNo")
                    return@withContext false
                }

                val documentName = queryResponse.data.first().document?.name ?: return@withContext false
                val userId = documentName.substringAfterLast("/")

                // Step 2: Prepare patch fields
                val updateFields = mapOf(
                    "password" to DatabaseValue.StringValue(updatedPass),
                    "updatedAt" to DatabaseValue.StringValue(Util.getCurrentTimeStamp())
                )

                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.USER_MASTER,
                    documentId = userId,
                    fields = updateFields.keys.toList()
                )

                // Step 3: Send PATCH request
                val patchResponse: HttpResponse = client.patch(patchUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(DatabaseRequest(fields = updateFields))
                }

                println("📬 PATCH Response: ${patchResponse.status}")
                return@withContext patchResponse.status.isSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                println("🚨 updateUserPassword error: ${e.message}")
                false
            }
        }
    }

    suspend fun sendRegistrationSms(phoneNumber: String): Boolean {
        val formattedPhoneNumber = phoneNumber.replace(Regex("^\\+"), "")
        val url = "https://control.msg91.com/api/v5/flow"

        val requestBody = SmsRequest(
            template_id = OtpAuthKeys.REGISTER_USER_TEMPLATE_ID,
            recipients = listOf(Recipient(mobiles = formattedPhoneNumber))
        )

        return try {
            val response: HttpResponse = client.post(url) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header("authkey", OtpAuthKeys.AUTH_KEY)
                setBody(Json.encodeToString(requestBody)) // 🔹 Fix: Serialize manually
            }

            val responseBody = response.bodyAsText()
            println("SMS Response: $responseBody")

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error sending SMS: ${e.message}")
            false
        }
    }

    @Serializable
    data class Recipient(val mobiles: String)

    @Serializable
    data class SmsRequest(
        val template_id: String,
        val short_url: String = "0",
        val realTimeResponse: String = "1",
        val recipients: List<Recipient>
    )

}
