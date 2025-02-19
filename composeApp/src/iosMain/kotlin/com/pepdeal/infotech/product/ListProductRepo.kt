package com.pepdeal.infotech.product

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toPixelMap
import coil3.Uri
import com.pepdeal.infotech.shop.modal.ProductMaster
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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.Foundation.*
import platform.darwin.*
import kotlinx.coroutines.*
import kotlinx.cinterop.*
import org.jetbrains.skia.Image
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsBeginImageContext
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation


class ListProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun addProductInTheShop(
        shopId: String,
        productMaster: ProductMaster,
        uriList: MutableList<ImageBitmap>,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            // Step 1: Check if the product already exists for the given shopId and productName
            val checkResponse: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json") {
                    parameter("orderBy", "\"shopId\"")
                    parameter("equalTo", "\"$shopId\"")
                    contentType(ContentType.Application.Json)
                }

            val checkBody = checkResponse.bodyAsText()
            println("Check Response: $checkBody") // Debug log

            if (checkBody.isNotEmpty() && checkBody != "{}") {
                val existingProducts = json.decodeFromString<Map<String, ProductMaster>>(checkBody)
                val productExists =
                    existingProducts.values.any { it.productName == productMaster.productName }

                if (productExists) {
                    onComplete(false, "Product already exists in this shop.")
                    return
                }
            }

            // Step 2: Generate a Product ID (Firebase push().key equivalent)
            val keyResponse: HttpResponse =
                client.post("${FirebaseUtil.BASE_URL}product_master.json") {
                    contentType(ContentType.Application.Json)
                    setBody("{}") // Firebase will generate a unique key
                }

            if (!keyResponse.status.isSuccess()) {
                onComplete(false, "Error while generating product ID.")
                return
            }

            val keyJson = keyResponse.body<Map<String, String>>()
            val productId = keyJson["name"] ?: run {
                onComplete(false, "Error while generating product ID.")
                return
            }

            println("Generated Product ID: $productId") // Debugging

            // Step 3: Upload Product Details
            val productWithId = productMaster.copy(productId = productId)

            val registerResponse: HttpResponse =
                client.put("${FirebaseUtil.BASE_URL}product_master/$productId.json") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(productWithId))
                }
            println(registerResponse)

            if (registerResponse.status == HttpStatusCode.OK) {
                println("product registration successful")
                onComplete(true, "Product registration successful.")
                // Step 4: Upload Images
                uploadImagesToFirebase(productId, uriList) { uploadSuccess, message ->
                    if (uploadSuccess) {
                        onComplete(true, "Product registered successfully.")
                    } else {
                        onComplete(false, message)
//                        Log.d("addProductInTheShop", "Product registered unsuccessfully.")
                    }
                }
            } else {
                onComplete(false, "Product registration failed.")
//                Log.d("addProductInTheShop", "Product registration failed.")
            }
        } catch (e: Exception) {
//            FirebaseCrashlytics.getInstance().recordException(e)
//            Log.d("addProductInTheShop", "Error: ${e.message}")
            onComplete(false, "Error registering product: ${e.message}")
        }
    }

    private suspend fun uploadImagesToFirebase(
        productId: String,
        images: List<ImageBitmap>,
        onComplete: (Boolean, String) -> Unit
    ) {
        val client = HttpClient(Darwin) // Use Darwin for iOS
        val bucketName = "pepdeal-1251f.appspot.com"

        try {
            coroutineScope {
                val uploadTasks = images.mapIndexed { index, imageBitmap ->
                    async {
                        val sanitizedProductId = productId.removePrefix("-")
                        val fileName = "product/$sanitizedProductId/image_$index.jpg"
                        val encodedPath = fileName.encodeURLPath()
                        val uploadUrl = "https://firebasestorage.googleapis.com/upload/storage/v0/b/$bucketName/o?uploadType=media&name=$encodedPath&location=asia-south1"

                        println("Starting upload for image $index...")
                        println("Upload URL: $uploadUrl")

                        val imageData = imageBitmap.toJpegNSData()?.toByteArray()
                        if (imageData == null || imageData.isEmpty()) {
                            println("Error: Failed to convert ImageBitmap to NSData for image $index")
                            return@async false
                        }

                        try {
                            val response: HttpResponse = client.post(uploadUrl) {
                                headers {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                }
                                setBody(imageData)
                            }

                            println("Response for image $index: ${response.status}")
                            val responseBody = response.bodyAsText()
                            println("Response Body for image $index: $responseBody")

                            response.status == HttpStatusCode.OK
                        } catch (e: Exception) {
                            println("Error uploading image $index: ${e.message}")
                            false
                        }
                    }
                }

                val results = uploadTasks.awaitAll() // Wait for all uploads to complete
                if (results.all { it }) {
                    println("✅ All images uploaded successfully!")
                    onComplete(true, "All images uploaded successfully!")
                } else {
                    println("❌ Some images failed to upload.")
                    onComplete(false, "Some images failed to upload.")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during image upload: ${e.message}")
            onComplete(false, "Error: ${e.message}")
        } finally {
            client.close()
            println("🔄 HTTP client closed.")
        }
    }


    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        return (this.bytes?.readBytes(this.length.toInt())) ?: ByteArray(0)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun ImageBitmap.toJpegNSData(): NSData? {
        UIGraphicsBeginImageContext(
            size = CGSizeMake(
                this.width.toDouble(),
                this.height.toDouble()
            )
        )
        val uiImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return uiImage?.let { UIImageJPEGRepresentation(it, 1.0) }
    }
}