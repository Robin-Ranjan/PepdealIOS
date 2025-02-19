package com.pepdeal.infotech.user

import com.pepdeal.infotech.UserProfilePicMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
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

    @OptIn(InternalAPI::class)
    suspend fun updateProfilePicture(
        userId: String,
        imageUri: String, // Path to the image file locally or a URI
        onComplete: (Boolean) -> Unit
    ) {
//        val client = HttpClient() // Ktor HttpClient
        val databaseUrl =
            "https://your-firebase-db-url.firebaseio.com/users.json" // Your Firebase DB URL
        val storageUrl = "https://your-firebase-storage-url.com" // Your Firebase Storage URL

        return withContext(Dispatchers.IO) {
            try {
                // Retrieve current user profile data from Firebase Realtime Database
                val snapshot: HttpResponse =
                    client.get("${FirebaseUtil.BASE_URL}user_profile_pic_master.json") {
                        parameter("orderBy", "\"userId\"")
                        parameter("equalTo", "\"$userId\"")
                        contentType(ContentType.Application.Json)
                    } // GET request for user data

                val oldProfilePicUrl = snapshot.bodyAsText()

                // Delete old profile picture if exists
                if (oldProfilePicUrl.isNotEmpty()) {
                    deleteOldProfilePicture(oldProfilePicUrl, client, storageUrl)
                }

                // Upload new profile picture and get the URL
//                val newProfilePicUrl = uploadProfilePicture(userId, imageUri, client, storageUrl)

                // Create data to update
                val updatedData = mapOf(
                    "id" to userId,
                    "userId" to userId,
                    "profilePicUrl" to "",
                    "updatedAt" to Util.getCurrentTimeStamp()
                )

                // Update user profile in Realtime Database
                val response: HttpResponse = client.put("$databaseUrl/$userId.json") {
                    contentType(ContentType.Application.Json)
                    body = updatedData
                }

                if (response.status == HttpStatusCode.OK) {
                    onComplete(true) // Successfully updated
                } else {
                    onComplete(false) // Error in update
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false) // Handle error
            } finally {
                client.close()
            }
        }
    }

    private suspend fun deleteOldProfilePicture(
        profilePicUrl: String,
        client: HttpClient,
        storageUrl: String
    ) {
        try {
            if (profilePicUrl.isNotEmpty()) {
                val deleteUrl =
                    "$storageUrl/$profilePicUrl" // Construct delete URL from the old profile picture URL
                val response = client.delete(deleteUrl) {
                    // Optionally pass authorization tokens if needed
                }
                if (response.status == HttpStatusCode.NoContent) {
                    println("Old profile picture deleted successfully.")
                } else {
                    println("Failed to delete old profile picture.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    private suspend fun uploadProfilePicture(
//        userId: String,
//        imageUri: String, // Image URI for uploading (could be local path or URI)
//        client: HttpClient,
//        storageUrl: String
//    ): String {
//        val fileName = "user_profile_pics/$userId/profile_${Util.getCurrentTimeStamp()}.jpg"
//        val fileUrl = "$storageUrl/$fileName"
//
//        try {
//            // Upload file as multipart/form-data using Ktor
//            val response = client.post("$storageUrl/upload") {
//                body = MultiPartFormDataContent(
//                    formData {
//                        append("file", File(imageUri).readBytes(), Headers.build {
//                            append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
//                            append(HttpHeaders.ContentDisposition, "filename=\"${fileName}\"")
//                        })
//                    }
//                )
//                // Check if upload was successful
//                if (response.status == HttpStatusCode.Created) {
//                    return fileUrl // Return the file URL upon successful upload
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return "" // Return empty string if upload fails
//    }


    suspend fun fetchUserProfilePic(userId: String): UserProfilePicMaster? {
        return try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}user_profile_pic_master.json") {
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


}