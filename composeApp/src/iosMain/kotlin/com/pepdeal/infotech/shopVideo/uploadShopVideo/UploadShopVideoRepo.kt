package com.pepdeal.infotech.shopVideo.uploadShopVideo

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.shopVideo.ShopVideosMaster
import com.pepdeal.infotech.shopVideo.ValidationResult
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
import io.ktor.client.request.parameter
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
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import kotlin.math.roundToInt

class UploadShopVideoRepo {

    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin)

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    suspend fun validateVideo(uri: String): ValidationResult {
        return withContext(Dispatchers.IO) {
            var sizeInMB = 0.0
            val durationInSeconds: Long

            // Check file size using NSFileManager
            val fileManager = NSFileManager.defaultManager
            val fileUrl = NSURL(string = uri)

            // Create a pointer for NSError using CPointer to Objective-C object
            val errorPointer = nativeHeap.alloc<ObjCObjectVar<NSError?>>()

            // Retrieve file attributes (size) with error handling
            val attributes = fileManager.attributesOfItemAtPath(fileUrl.path ?: "", errorPointer.ptr)

            // Check if there was an error
            val error = errorPointer.value
            if (error != null) {
                nativeHeap.free(errorPointer) // Free the allocated memory
                return@withContext ValidationResult(false, "Error accessing file attributes: ${error.localizedDescription}")
            }

            // Ensure that file exists and fetch size
            val fileSize = attributes?.get(NSFileSize) as? Long ?: 0L
            sizeInMB = fileSize / (1024.0 * 1024.0)

            // Correct initialization of AVURLAsset
            val asset = AVURLAsset(uRL = fileUrl, options = null)  // Use the correct constructor with URL and optional options

            // Access the duration using CMTime
            // Access the duration using CMTimeGetSeconds
            val duration = CMTimeGetSeconds(asset.duration)
             durationInSeconds = duration.toLong()  // Convert to Long

            // Validate the video properties
            if (sizeInMB > 10) {
                nativeHeap.free(errorPointer) // Free the allocated memory
                return@withContext ValidationResult(false, "File size exceeds 10 MB. ${sizeInMB.roundToInt()}")
            }
            if (durationInSeconds > 60) {
                nativeHeap.free(errorPointer) // Free the allocated memory
                return@withContext ValidationResult(false, "Video duration exceeds 1 minute. $duration")
            }

            nativeHeap.free(errorPointer) // Free the allocated memory
            ValidationResult(true, "Video is valid.")
        }
    }

    suspend fun getShopVideoWithThumbNail(
        shopId: String,
        onSuccess: (ShopVideosMaster?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val response: HttpResponse = client.get("${FirebaseUtil.BASE_URL}shops_videos_master.json") {
                parameter("orderBy", "\"shopId\"")
                parameter("equalTo", "\"$shopId\"")
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val shopVideoMap: Map<String, ShopVideosMaster> = json.decodeFromString(response.bodyAsText())
                onSuccess(shopVideoMap.values.firstOrNull())
            }else{
                onFailure("Not Found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.message.toString())
        }
    }

    suspend fun uploadVideoWithDelete(
        byteArray: ByteArray,
        thumbNailImage:ImageBitmap?,
        shopId: String,
        onSuccess: (String) -> Unit,
        onProgress:(String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val bucket = FirebaseUtil.BUCKET_URL
        val folderPath = "shop_videos_master/$shopId/"
        val fileName = "$folderPath${Util.getCurrentTimeStamp()}.mp4"

        // Step 1: Delete existing video(s) if any
        deleteExistingVideosIfAny(bucket, shopId)

        // Step 2: Upload new video
        val downloadUrl = uploadVideoUsingKtor(bucket, fileName, byteArray, "video/mp4")

        if (downloadUrl != null) {
            println("✅ Video uploaded successfully: $downloadUrl")

            // Step 3: Update or create the shop video record
            updateOrCreateShopVideo(shopId, downloadUrl)

            thumbNailImage?.let {
                uploadThumbnailWithDelete(shopId, imageValue = thumbNailImage)
            }?: println("thumbnail image is null")

        } else {
            onFailure("❌ Video upload failed.")
        }
    }

    private suspend fun uploadVideoUsingKtor(
        bucket: String,
        fileName: String,
        byteArray: ByteArray,
        contentType: String
    ): String? {
        val client = HttpClient(Darwin)
        val url = "https://firebasestorage.googleapis.com/v0/b/$bucket/o?uploadType=media&name=$fileName"

        return try {
            val response: HttpResponse = client.post(url) {
                header(HttpHeaders.ContentType, contentType)
                setBody(byteArray)
            }
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                return getDownloadUrl(responseText)
            } else {
                println("❌ Upload failed: ${response.status.value} - ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Exception during video upload: ${e.message}")
            e.printStackTrace()
            null
        } finally {
            client.close()
        }
    }

    private suspend fun deleteExistingVideosIfAny(bucket: String, shopId: String) {
        val client = HttpClient(Darwin)
        try {
            val listUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o?prefix=shop_videos_master/$shopId/"
            val listResponse: HttpResponse = client.get(listUrl)

            if (listResponse.status.isSuccess()) {
                val responseText = listResponse.bodyAsText()
                val json = Json.parseToJsonElement(responseText).jsonObject
                val items = json["items"]?.jsonArray ?: return

                if (items.isEmpty()) {
                    println("ℹ️ No existing videos found for shop: $shopId")
                    return
                }

                for (item in items) {
                    val fullPath = item.jsonObject["name"]?.jsonPrimitive?.content ?: continue
                    val encodedPath = fullPath.replace("/", "%2F")

                    val deleteUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath"

                    val deleteResponse: HttpResponse = client.request(deleteUrl) {
                        method = HttpMethod.Delete
                    }

                    if (deleteResponse.status.isSuccess()) {
                        println("✅ Deleted: $fullPath")
                    } else {
                        println("❌ Failed to delete: $fullPath, Status: ${deleteResponse.status}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Error deleting video: ${e.message}")
        } finally {
            client.close()
        }
    }

    private fun getDownloadUrl(responseJson: String): String? {
        return try {
            val uploadResponse = json.decodeFromString(FirebaseUploadResponse.serializer(), responseJson)
            val token = uploadResponse.downloadTokens
            if (token.isNullOrEmpty()) {
                println("❌ No download token found.")
                null
            } else {
                "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/${uploadResponse.name.replace("/", "%2F")}?alt=media&token=$token"
            }
        } catch (e: Exception) {
            println("❌ Error parsing upload response: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend fun updateOrCreateShopVideo(shopId: String, downloadUrl: String) {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        try {
            val shopVideoUrl = "${FirebaseUtil.BASE_URL}shops_videos_master.json?orderBy=\"shopId\"&equalTo=\"$shopId\""
            val response: HttpResponse = client.get(shopVideoUrl)

            if (response.status.isSuccess()) {
                val data = response.body<Map<String, ShopVideosMaster>>()

                // Check if a video record already exists for this shop
                val existingEntry = data.entries.find { it.value.shopId == shopId }

                if (existingEntry != null) {
                    val videoId = existingEntry.key
                    val updatedVideo = existingEntry.value.copy(
                        videoUrl = downloadUrl,
                        updatedAt = Util.getCurrentTimeStamp()
                    )

                    // Update the existing entry
                    val updateResponse: HttpResponse =
                        client.put("${FirebaseUtil.BASE_URL}shops_videos_master/$videoId.json") {
                            contentType(ContentType.Application.Json)
                            setBody(json.encodeToString(updatedVideo))
                        }

                    if (updateResponse.status == HttpStatusCode.OK) {
                        println("✅ Updated video URL for shop: $shopId")
                    } else {
                        println("❌ Failed to update video URL.")
                    }
                } else {
                    // No existing entry, create a new one
                    val newShopVideo = ShopVideosMaster(
                        shopVideoId = "",
                        shopId = shopId,
                        videoUrl = downloadUrl,
                        thumbNailUrl = "",
                        createdAt = Util.getCurrentTimeStamp(),
                        updatedAt = Util.getCurrentTimeStamp()
                    )

                    val keyResponse: HttpResponse =
                        client.post("${FirebaseUtil.BASE_URL}shops_videos_master.json") {
                            contentType(ContentType.Application.Json)
                            setBody("{}")
                        }

                    if (keyResponse.status.isSuccess()) {
                        val keyJson = keyResponse.body<Map<String, String>>()
                        val id = keyJson["name"] ?: return

                        val shopVideoWithId = newShopVideo.copy(shopVideoId = id)

                        val registerResponse: HttpResponse =
                            client.put("${FirebaseUtil.BASE_URL}shops_videos_master/$id.json") {
                                contentType(ContentType.Application.Json)
                                setBody(json.encodeToString(shopVideoWithId))
                            }

                        if (registerResponse.status == HttpStatusCode.OK) {
                            println("✅ New video entry created for shop: $shopId")
                        } else {
                            println("❌ Failed to create video entry.")
                        }
                    } else {
                        println("❌ Error creating video id: ${keyResponse.bodyAsText()}")
                    }
                }
            } else {
                println("❌ Failed to fetch shop videos.")
            }
        } catch (e: Exception) {
            println("❌ Exception updating/creating shop video: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    private suspend fun uploadThumbnailWithDelete(shopId: String, imageValue: ImageBitmap) {
        val client = HttpClient(Darwin)

        try {
            // ✅ Step 1: List and Delete Existing Thumbnail from Firebase Storage
            val listUrl =
                "https://firebasestorage.googleapis.com/v0/b/${FirebaseUtil.BUCKET_URL}/o?prefix=shop_videos_thumbNail_master/$shopId/"
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
                        println("✅ Deleted old thumbnail: $fullPath")
                    } else {
                        println("❌ Failed to delete thumbnail: $fullPath, Status: ${deleteResponse.status}")
                    }
                }
            } else {
                println("❌ Failed to list thumbnails. Status: ${listResponse.status}")
            }

            // ✅ Step 3: Upload New Thumbnail to Firebase Storage
            val newThumbnailUrl = uploadThumbNailToFirebase(shopId, listOf(imageValue) )
            if (newThumbnailUrl.isNullOrEmpty()) {
                println("❌ Thumbnail upload failed.")
                return
            }

            // ✅ Step 4: Update or Create Thumbnail Entry in shop_videos_master
            val videoMasterUrl = "${FirebaseUtil.BASE_URL}shops_videos_master.json"
            val videoResponse: HttpResponse = client.get(videoMasterUrl)

            val videoData: Map<String, ShopVideosMaster> = if (videoResponse.status.isSuccess()) {
                val responseBody = videoResponse.bodyAsText()
                if (responseBody.isNotEmpty() && responseBody != "null") {
                    json.decodeFromString(responseBody)
                } else emptyMap()
            } else emptyMap()

            val existingVideoEntry = videoData.entries.find { it.value.shopId == shopId }

            if (existingVideoEntry != null) {
                val videoId = existingVideoEntry.key
                val updatedVideo = existingVideoEntry.value.copy(
                    thumbNailUrl = newThumbnailUrl,
                    updatedAt = Util.getCurrentTimeStamp()
                )

                val updateResponse: HttpResponse = client.put("${FirebaseUtil.BASE_URL}shops_videos_master/$videoId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(updatedVideo))
                }

                if (updateResponse.status == HttpStatusCode.OK) {
                    println("✅ Updated thumbnail URL for shop video: $shopId")
                } else {
                    println("❌ Failed to update thumbnail URL.")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during thumbnail upload: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    private suspend fun uploadThumbNailToFirebase(
        shopId: String,
        images: List<ImageBitmap>
    ):  String? {
        val bucket = FirebaseUtil.BUCKET_URL
        val baseFileName = "shop_videos_thumbNail_master/${shopId}/${Util.getCurrentTimeStamp()}"

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
                    val imageName = "image$index"

                    // Upload the image using Ktor and get the download URL.
                    val downloadUrl = uploadThumbNailUsingKtor(
                        shopId,
                        bucket,
                        fileName,
                        imageData,
                        "image/jpeg",
                        imageName
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
        // Aggregate the results into one overall status and a combined message.
        val overallSuccess = results.all { it.first }
        val combinedMessage = results.joinToString(separator = "; ") { it.second }
        return null
    }

    // ------------------------------
// STEP 3: Revised uploadImageUsingKtor function remains similar.
// It uploads an image via Ktor and returns the download URL if successful.
// ------------------------------
    private suspend fun uploadThumbNailUsingKtor(
        shopId: String,
        bucket: String,
        fileName: String,
        imageData: ByteArray,
        contentType: String = "image/jpeg",
        imageName: String
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
                return getDownloadThumbNailUrl(responseText, shopId, imageName)
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

    // ------------------------------
// STEP 4: Revised getDownloadUrl helper
// Constructs the download URL based on the response JSON and fixed format.
// ------------------------------
    private fun getDownloadThumbNailUrl(
        responseJson: String,
        shopId: String,
        imageName: String
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
            println("https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/shop_videos_thumbNail_master%2F$shopId%2F$name?alt=media&token=$token")
            return "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/shop_videos_thumbNail_master%2F$shopId%2F$name?alt=media&token=$token"
        } catch (e: Exception) {
            println("Error parsing upload response: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}