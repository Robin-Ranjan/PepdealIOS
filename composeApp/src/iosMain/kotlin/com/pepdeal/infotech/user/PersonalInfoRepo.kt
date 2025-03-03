package com.pepdeal.infotech.user

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.UserProfilePicMaster
import com.pepdeal.infotech.shop.modal.ShopMaster
import com.pepdeal.infotech.shopVideo.ShopVideosMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.ImagesUtil.toByteArray
import com.pepdeal.infotech.util.ImagesUtil.toNSData
import com.pepdeal.infotech.util.ImagesUtil.toUIImage
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PersonalInfoRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun updateUserEmailId(userId: String, updatedUserEmail: String): Boolean {
        return try {
            val httpClient = HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

            val requestBody = mapOf(
                "emailId" to updatedUserEmail,
                "updatedAt" to Util.getCurrentTimeStamp()
            )

            val response: HttpResponse =
                httpClient.patch("${FirebaseUtil.BASE_URL}user_master/$userId.json") {
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

    suspend fun fetchUserProfilePic(userId: String): UserProfilePicMaster? {
        return try {
            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}user_profile_pic_master.json") {
                    parameter("orderBy", "\"userId\"")
                    parameter("equalTo", "\"$userId\"")
                    contentType(ContentType.Application.Json)
                }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                println("Response: $responseBody") // Debugging Log

                if (responseBody.isEmpty() || responseBody == "{}") {
                    println("No profile picture found for userId: $userId")
                    return null
                }

                val result: Map<String, UserProfilePicMaster> = json.decodeFromString(responseBody)
                val profilePic = result.values.firstOrNull() // Get the first value in the map

                if (profilePic == null) {
                    println("Parsed response is empty for userId: $userId")
                }

                profilePic
            } else {
                println("Failed to fetch profile picture: ${response.status}")
                println("Response Body: ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("Error fetching profile picture: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchShopId(shopMobileNo: String): String? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shop_master.json") {
                parameter("orderBy", "\"shopMobileNo\"")
                parameter("equalTo", "\"$shopMobileNo\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                println("Response: $responseBody") // Debugging Log

                if (responseBody.isEmpty() || responseBody == "{}") {
                    println("No profile picture found for userId: $shopMobileNo")
                    return null
                }

                val result: Map<String, ShopMaster> = json.decodeFromString(responseBody)
                val profilePic = result.values.firstOrNull() // Get the first value in the map

                if (profilePic == null) {
                    println("Parsed response is empty for userId: $shopMobileNo")
                }

                profilePic?.shopId
            } else {
                println("Failed to fetch profile picture: ${response.status}")
                println("Response Body: ${response.bodyAsText()}")
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            null
        }
    }

    suspend fun uploadThumbnailWithDelete(userId: String, imageValue: ImageBitmap) {
        val client = HttpClient(Darwin)

        try {
            // ✅ Step 1: List and Delete Existing Thumbnail from Firebase Storage
            val listUrl =
                "https://firebasestorage.googleapis.com/v0/b/${FirebaseUtil.BUCKET_URL}/o?prefix=user_profile_pics/$userId/"
            val listResponse: HttpResponse = client.get(listUrl)

            if (listResponse.status.isSuccess()) {
                val responseText = listResponse.bodyAsText()
                val json = Json.parseToJsonElement(responseText).jsonObject
                val items = json["items"]?.jsonArray ?: return

                for (item in items) {
                    val fullPath = item.jsonObject["name"]?.jsonPrimitive?.content ?: continue
                    val encodedPath = fullPath.replace("/", "%2F")

                    val deleteUrl =
                        "https://firebasestorage.googleapis.com/v0/b/${FirebaseUtil.BUCKET_URL}/o/$encodedPath"
                    val deleteResponse: HttpResponse = client.request(deleteUrl) {
                        method = HttpMethod.Delete
                    }

                    if (deleteResponse.status.isSuccess()) {
                        println("✅ Deleted old profile pic: $fullPath")
                    } else {
                        println("❌ Failed to delete profile pic: $fullPath, Status: ${deleteResponse.status}")
                    }
                }
            } else {
                println("❌ Failed to list profile pic. Status: ${listResponse.status}")
            }

            // ✅ Step 3: Upload New Thumbnail to Firebase Storage
            val newProfilePicUrl = uploadProfilePicToFirebase(userId, listOf(imageValue))
            if (newProfilePicUrl.isNullOrEmpty()) {
                println("❌ profile pic upload failed.")
                return
            }

            // ✅ Step 4: Update or Create Thumbnail Entry in user_profile_pic_master
            val userProfilePicMasterUrl = "${FirebaseUtil.BASE_URL}user_profile_pic_master.json"
            val videoResponse: HttpResponse = client.get(userProfilePicMasterUrl)

            val profilePicData: Map<String, UserProfilePicMaster> =
                if (videoResponse.status.isSuccess()) {
                    val responseBody = videoResponse.bodyAsText()
                    if (responseBody.isNotEmpty() && responseBody != "null") {
                        json.decodeFromString(responseBody)
                    } else emptyMap()
                } else emptyMap()

            val existingProfilePicEntry = profilePicData.entries.find { it.value.userId == userId }

            if (existingProfilePicEntry != null) {
                val profilePicId = existingProfilePicEntry.key
                val updatedProfilePic = existingProfilePicEntry.value.copy(
                    profilePicUrl = newProfilePicUrl,
                    updatedAt = Util.getCurrentTimeStamp()
                )

                val updateResponse: HttpResponse =
                    client.put("${FirebaseUtil.BASE_URL}user_profile_pic_master/$profilePicId.json") {
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(updatedProfilePic))
                    }

                if (updateResponse.status == HttpStatusCode.OK) {
                    println("✅ Updated profile pic URL for user: $userId")
                } else {
                    println("❌ Failed to update profile pic URL.")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during profile pic upload: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    private suspend fun uploadProfilePicToFirebase(
        userId: String,
        images: List<ImageBitmap>
    ): String? {
        val bucket = FirebaseUtil.BUCKET_URL
        val baseFileName = "user_profile_pics/${userId}/${Util.getCurrentTimeStamp()}"

        // Map each image to a Pair<Boolean, String> outcome.
        val results = images.mapIndexed { index, imageBitmap ->
            try {
                // Convert the ImageBitmap to NSData and then to ByteArray
                val nsData = imageBitmap.toUIImage()?.toNSData()
                if (nsData == null || nsData.length.toInt() == 0) {
                    println("Error: Conversion failed for image $index")
                    Pair(false, "Conversion failed for image $index")
                } else {
                    val imageData = nsData.toByteArray()
                    val fileName = "$baseFileName$index.jpg"

                    // Upload the image using Ktor and get the download URL.
                    val downloadUrl = uploadProfilePicUsingKtor(
                        userId,
                        bucket,
                        fileName,
                        imageData,
                        "image/jpeg"
                    )
                    if (downloadUrl != null) {
                        return downloadUrl
                    } else {
                        println("❌ Error uploading image $index: Response is null")
                        Pair(false, "Error uploading image $index")
                    }
                }
            } catch (e: Exception) {
                println("❌ Exception uploading image $index: ${e.message}")
                e.printStackTrace()
                Pair(false, "Exception uploading image $index: ${e.message}")
            }
        }
        return null
    }

    // STEP 3: Revised uploadImageUsingKtor function remains similar.
// It uploads an image via Ktor and returns the download URL if successful.
// ------------------------------
    private suspend fun uploadProfilePicUsingKtor(
        userId: String,
        bucket: String,
        fileName: String,
        imageData: ByteArray,
        contentType: String = "image/jpeg"
    ): String? {
        val client = HttpClient(Darwin)
        // Construct the upload URL using the file name (assumed to be URL-safe or already encoded)
        val url =
            "https://firebasestorage.googleapis.com/v0/b/$bucket/o?uploadType=media&name=${fileName}"
        try {
            // Perform a POST request with the image data.
            val response: HttpResponse = client.post(url) {
                header(HttpHeaders.ContentType, contentType)
                setBody(imageData)
            }
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                return getDownloadThumbNailUrl(responseText, userId)
            } else {
                println("Upload failed with status ${response.status.value}: ${response.bodyAsText()}")
                return null
            }
        } catch (e: Exception) {
            println("Exception during upload: ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            client.close()
        }
    }

    // STEP 4: Revised getDownloadUrl helper
// Constructs the download URL based on the response JSON and fixed format.
// ------------------------------
    private fun getDownloadThumbNailUrl(
        responseJson: String,
        userId: String,
    ): String? {
        try {
            val uploadResponse =
                json.decodeFromString(FirebaseUploadResponse.serializer(), responseJson)
            val token = uploadResponse.downloadTokens
            val name = uploadResponse.name.substringAfterLast("/")
            if (token.isNullOrEmpty()) {
                println("No download token available in the response")
                return null
            }
            // Construct the URL using the fixed format and URL-encoded file path
            println("https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/user_profile_pics%2F$userId%2F$name?alt=media&token=$token")
            return "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/user_profile_pics%2F$userId%2F$name?alt=media&token=$token"
        } catch (e: Exception) {
            println("Error parsing upload response: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}