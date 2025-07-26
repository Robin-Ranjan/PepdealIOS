package com.pepdeal.infotech.user.repository

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.UserProfilePicMaster
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
import com.pepdeal.infotech.core.domain.onError
import com.pepdeal.infotech.core.domain.onSuccess
import com.pepdeal.infotech.core.utils.AppJson
import com.pepdeal.infotech.placeAPI.httpClient
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.FirebaseUtil
import com.pepdeal.infotech.util.ImagesUtil.toByteArray
import com.pepdeal.infotech.util.ImagesUtil.toNSData
import com.pepdeal.infotech.util.ImagesUtil.toUIImage
import com.pepdeal.infotech.util.Util
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PersonalInfoRepo {
    val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }

    suspend fun updateUserEmailId(userId: String, updatedUserEmail: String): Boolean {
        return try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.USER_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId)
                ),
                limit = 1
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            if (response is AppResult.Success && response.data.isNotEmpty()) {
                val documentName = response.data.first().document?.name ?: return false
                val documentId = documentName.substringAfterLast("/")

                // Step 3: Prepare updates
                val updateFields = mapOf(
                    "emailId" to DatabaseValue.StringValue(updatedUserEmail),
                    "updatedAt" to DatabaseValue.StringValue(Util.getCurrentTimeStamp())
                )

                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.USER_MASTER,
                    documentId = documentId,
                    fields = updateFields.keys.toList()
                )

                // Step 4: PATCH to Firestore
                val patchResponse =
                    client.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(DatabaseRequest(fields = updateFields))
                    }

                return patchResponse.status.isSuccess()
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchUserDetails2(userId: String): UserMaster {
        return try {

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.USER_MASTER,
                limit = 1,
                filters = listOf(
                    FirestoreFilter("userId", userId),
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Error -> UserMaster()

                is AppResult.Success -> {
                    val user = response.data.firstOrNull()?.document?.fields?.let { fields ->
                        UserMaster(
                            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userName = (fields["userName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mobileNo = (fields["mobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            emailId = (fields["emailId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            password = (fields["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fcmToken = (fields["fcmToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            deviceToken = (fields["deviceToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userStatus = (fields["userStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                    }
                    return user ?: UserMaster()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UserMaster()
        }
    }

    suspend fun fetchUserProfilePic(userId: String): UserProfilePicMaster? {
        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.USER_PROFILE_PIC_MASTER,
                filters = listOf(
                    FirestoreFilter(field = "userId", value = userId)
                ),
                limit = 1
            )

            val response: HttpResponse = client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                val queryResult: List<DatabaseQueryResponse> =
                    AppJson.decodeFromString(responseBody)

                val fields = queryResult.firstOrNull()?.document?.fields
                if (fields != null) {
                    val profilePic = UserProfilePicMaster(
                        id = (fields["id"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        profilePicUrl = (fields["profilePicUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                    )
                    return profilePic
                } else {
                    val errorMessage = "No profile picture found for userId: $userId"
                    println(errorMessage)
                    return null
                }
            } else {
                val errorMessage =
                    "Failed with status: ${response.status.value} - ${response.status.description}"
                println(errorMessage)
                println("Body: ${response.bodyAsText()}")
                return null
            }

        } catch (e: Exception) {
            val errorMessage = "Exception during fetchUserProfilePic: ${e.message}"
            println(errorMessage)
            e.printStackTrace()
            return null
        }
    }

    suspend fun fetchShopId(shopMobileNo: String): String? {
        return try {

            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.SHOP_MASTER,
                limit = 1,
                filters = listOf(
                    FirestoreFilter("shopMobileNo", shopMobileNo),
                )
            )

            val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }.body()
            }

            when (response) {
                is AppResult.Error -> null

                is AppResult.Success -> {
                    val shopId = response.data.firstOrNull()?.document?.fields?.let { fields ->
                        (fields["shopId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()

                    }
                    return shopId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            null
        }
    }

    suspend fun updateProfilePic(userId: String, imageValue: ImageBitmap) {
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
            updateOrCreateUserProfilePicFirestore(
                userId = userId,
                newProfilePicUrl = newProfilePicUrl
            )
                .onSuccess { println("✅ Updated profile pic URL for user: $userId") }
                .onError { println("❌ Failed to update profile pic URL.") }

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
                AppJson.decodeFromString(FirebaseUploadResponse.serializer(), responseJson)
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


    private suspend fun updateOrCreateUserProfilePicFirestore(
        userId: String,
        newProfilePicUrl: String
    ): AppResult<Unit, DataError.Remote> {
        return try {
            // Step 1: Query Firestore for existing entry with userId
            val query = buildFirestoreQuery(
                collection = DatabaseCollection.USER_PROFILE_PIC_MASTER,
                filters = listOf(
                    FirestoreFilter("userId", userId)
                ),
                limit = 1
            )

            val queryResult: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                client.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(query)
                }.body()
            }

            if (queryResult is AppResult.Error) {
                return AppResult.Error(
                    DataError.Remote(
                        type = DataError.RemoteType.SERVER,
                        message = "❌ Failed to query profile pic: ${queryResult.error.message}"
                    )
                )
            }

            val documents = (queryResult as AppResult.Success).data.mapNotNull { it.document }
            val timestamp = Util.getCurrentTimeStamp()

            if (documents.isNotEmpty()) {
                // Step 2a: Update existing document
                val doc = documents.first()
                val documentId = doc.name.substringAfterLast("/")

                val updateFields = mapOf(
                    "profilePicUrl" to DatabaseValue.StringValue(newProfilePicUrl),
                    "updatedAt" to DatabaseValue.StringValue(timestamp)

                )
                val patchUrl = buildFirestorePatchUrl(
                    collection = DatabaseCollection.USER_PROFILE_PIC_MASTER,
                    documentId = documentId,
                    fields = updateFields.keys.toList()
                )

                val updateResult: AppResult<HttpResponse, DataError.Remote> = safeCall {
                    client.patch(patchUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(DatabaseRequest(fields = updateFields))
                    }
                }

                return if (updateResult is AppResult.Success && updateResult.data.status.isSuccess()) {
                    println("✅ Updated profile pic for userId = $userId")
                    AppResult.Success(Unit)
                } else {
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = "❌ Failed to update profile pic: ${
                                (updateResult as? AppResult.Error)?.error?.message ?: "Unknown error"
                            }"
                        )
                    )
                }
            } else {
                // Step 2b: Create new document
                val newEntry = UserProfilePicMaster(
                    userId = userId,
                    profilePicUrl = newProfilePicUrl,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )

                val createResult =
                    client.post("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USER_PROFILE_PIC_MASTER}") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "userId" to DatabaseValue.StringValue(newEntry.userId),
                                    "profilePicUrl" to DatabaseValue.StringValue(newEntry.profilePicUrl),
                                    "createdAt" to DatabaseValue.StringValue(newEntry.createdAt),
                                    "updatedAt" to DatabaseValue.StringValue(newEntry.updatedAt)
                                )
                            )
                        )
                    }


                if (!createResult.status.isSuccess()) {
                    println("❌ Failed to create new document")
                    return AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = "❌ Failed to create profile pic: ${createResult.status}"
                        )
                    )
                }

                val databaseResponse: DatabaseResponse = createResult.body()
                val generatedId = databaseResponse.name.substringAfterLast("/")

                val patchResponse =
                    httpClient.patch("${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.USER_PROFILE_PIC_MASTER}/$generatedId?updateMask.fieldPaths=id") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "id" to DatabaseValue.StringValue(generatedId)
                                )
                            )
                        )
                    }

                return if (patchResponse.status.isSuccess()) {
                    println("✅ Created new profile pic entry for userId = $userId")
                    AppResult.Success(Unit)
                } else {
                    AppResult.Error(
                        DataError.Remote(
                            type = DataError.RemoteType.SERVER,
                            message = "❌ Failed to create profile pic: ${patchResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.SERVER,
                    message = "🔥 Exception: ${e.message}"
                )
            )
        }
    }

}
