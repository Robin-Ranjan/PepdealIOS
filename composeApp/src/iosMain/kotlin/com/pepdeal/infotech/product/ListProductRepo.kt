package com.pepdeal.infotech.product

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.shop.modal.ProductMaster
import com.pepdeal.infotech.util.FirebaseUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
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
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.refTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Foundation.NSData
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
                    }
                }
            } else {
                onComplete(false, "Product registration failed.")
            }
        } catch (e: Exception) {
            onComplete(false, "Error registering product: ${e.message}")
        }
    }

    private suspend fun uploadImagesToFirebase(
        productId: String,
        images: List<ImageBitmap>,
        onComplete: (Boolean, String) -> Unit
    ) {
        val bucket = "pepdeal-1251f.appspot.com"
        val baseFileName = "product/${productId}/image"

        // For each image, perform the upload.
        val results = images.mapIndexed { index, imageBitmap ->
            try {
                val nsData = imageBitmap.toUIImage()?.toNSData()
                if (nsData == null || nsData.length.toInt() == 0) {
                    println("Error: Conversion failed for image $index")
                    false
                } else {
                    val imageData = nsData.toByteArray()
                    val fileName = "$baseFileName$index.jpg"
                    val imageName = "image$index"

                    val downloadUrl = uploadImageUsingKtor(
                        productId,
                        bucket,
                        fileName,
                        imageData,
                        "image/jpeg",
                        imageName
                    )
                    if (downloadUrl != null) {
                        println("✅ Image $index uploaded successfully")
                        println(downloadUrl)
                        true
                    } else {
                        println("❌ Error uploading image $index: Response is null")
                        false
                    }
                }
            } catch (e: Exception) {
                println("❌ Exception uploading image $index: ${e.message}")
                e.printStackTrace()
                false
            }
        }

        if (results.all { it }) {
            onComplete(true, "All images uploaded successfully!")
        } else {
            onComplete(false, "Some images failed to upload.")
        }
    }

    // This function uploads the image data to Firebase Storage using REST API.
    private suspend fun uploadImageUsingKtor(
        productId: String,
        bucket: String,
        fileName: String,
        imageData: ByteArray,
        contentType: String = "image/jpeg",
        imageName: String
    ): String? {
        val client = HttpClient(Darwin)
        // Construct the upload URL:
        val url =
            "https://firebasestorage.googleapis.com/v0/b/$bucket/o?uploadType=media&name=${fileName}"
        try {
            // Perform a POST request with the image data as the body.
            val response: HttpResponse = client.post(url) {
                header(HttpHeaders.ContentType, contentType)
                // Use the raw ByteArray as the request body.
                setBody(imageData)
            }
            // Check for a successful HTTP status code (200-299)
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                println("Upload successful: $responseText")
                return getDownloadUrl(responseText, productId, imageName)
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

    private fun getDownloadUrl(
        responseJson: String,
        productId: String,
        imageName: String
    ): String? {
        val json = Json { ignoreUnknownKeys = true }
        try {
            val uploadResponse =
                json.decodeFromString(FirebaseUploadResponse.serializer(), responseJson)
            val token = uploadResponse.downloadTokens
            if (token.isNullOrEmpty()) {
                println("No download token available in the response")
                return null
            }
            val imageUrl =
                "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/product%2F$productId%2F$imageName.jpg?alt=media&token=${uploadResponse.downloadTokens}"
            return imageUrl
        } catch (e: Exception) {
            println("Error parsing upload response: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun UIImage.toNSData(): NSData? {
        return UIImageJPEGRepresentation(this, 0.8)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        return (this.bytes?.readBytes(this.length.toInt())) ?: ByteArray(0)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun ImageBitmap.toUIImage(): UIImage? {
        val width = this.width
        val height = this.height
        val buffer = IntArray(width * height)

        this.readPixels(buffer)

        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val context = CGBitmapContextCreate(
            data = buffer.refTo(0),
            width = width.toULong(),
            height = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = (4 * width).toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        )

        val cgImage = CGBitmapContextCreateImage(context)
        return cgImage?.let { UIImage.imageWithCGImage(it) }
    }
}
