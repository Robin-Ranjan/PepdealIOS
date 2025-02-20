package com.pepdeal.infotech.product

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.util.FirebaseUtil
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

    // ------------------------------
// STEP 1: Main function to add product in the shop.
// This function aggregates all messages and calls onComplete only once.
// ------------------------------
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
//            println("Register response: $registerResponse")

            if (registerResponse.status != HttpStatusCode.OK) {
                onComplete(false, "Product registration failed.")
                return
            }
            println("Product registration successful.")

            // Step 4: Upload Images and collect overall status and message.
            val (imagesUploaded, imageMsg) = uploadImagesToFirebase(productId, uriList)

            // Final aggregation: If product details were registered and images uploaded successfully, then overall success.
            if (imagesUploaded) {
                onComplete(
                    true,
                    "Product registered successfully and all images uploaded successfully."
                )
            } else {
                onComplete(
                    false,
                    "Product registered but some images failed to upload. Details: $imageMsg"
                )
            }
        } catch (e: Exception) {
            // Single error message returned at the end
            onComplete(false, "Error registering product: ${e.message}")
        } finally {
            client.close()
        }
    }

    // ------------------------------
// STEP 2: Revised uploadImagesToFirebase
// Changed from using an onComplete callback to returning a Pair<Boolean, String> for overall result.
// ------------------------------
    private suspend fun uploadImagesToFirebase(
        productId: String,
        images: List<ImageBitmap>
    ): Pair<Boolean, String> {
        val bucket = "pepdeal-1251f.appspot.com"
        val baseFileName = "product/${productId}/image"

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

                        // Create a new entry in the realtime database for this image.
                        val productImage = ProductImageMaster(
                            id = "",
                            productId = productId,
                            productImages = downloadUrl,
                            createdAt = Util.getCurrentTimeStamp(),
                            updatedAt = Util.getCurrentTimeStamp()
                        )
                        // This function updates the database node with the product image.
                        createNewProductImageId(productImage)
                        Pair(true, "Image $index uploaded successfully")
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
        return Pair(overallSuccess, combinedMessage)
    }

    // ------------------------------
// STEP 3: Revised uploadImageUsingKtor function remains similar.
// It uploads an image via Ktor and returns the download URL if successful.
// ------------------------------
    private suspend fun uploadImageUsingKtor(
        productId: String,
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

    // ------------------------------
// STEP 4: Revised getDownloadUrl helper
// Constructs the download URL based on the response JSON and fixed format.
// ------------------------------
    private fun getDownloadUrl(
        responseJson: String,
        productId: String,
        imageName: String
    ): String? {
        try {
            val uploadResponse =
                json.decodeFromString(FirebaseUploadResponse.serializer(), responseJson)
            val token = uploadResponse.downloadTokens
            if (token.isNullOrEmpty()) {
                println("No download token available in the response")
                return null
            }
            // Construct the URL using the fixed format and URL-encoded file path
            return "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/product%2F$productId%2F$imageName.jpg?alt=media&token=$token"
        } catch (e: Exception) {
            println("Error parsing upload response: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    // ------------------------------
// STEP 5: Revised createNewProductImageId function that updates the realtime DB node.
// It uses the ProductImageMaster object and does not call any callback directly.
// ------------------------------
    private suspend fun createNewProductImageId(productImage: ProductImageMaster) {
        try {
             val client = HttpClient(Darwin) {
                install(ContentNegotiation) {
                    json(json)
                }
            }
            val keyResponse: HttpResponse =
                client.post("${FirebaseUtil.BASE_URL}product_images_master.json") {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                }
            if (keyResponse.status.isSuccess()) {
                val keyJson = keyResponse.body<Map<String, String>>()
                val id = keyJson["name"] ?: run {
                    println("Error while generating product image ID.")
                    return
                }
                // Update the product image object with the new ID.
                val productImageWithId = productImage.copy(id = id)
                val registerResponse: HttpResponse =
                    client.put("${FirebaseUtil.BASE_URL}product_images_master/$id.json") {
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(productImageWithId))
                    }
                if (registerResponse.status == HttpStatusCode.OK) {
                    println("Product image updated successfully with id: $id")
                } else {
                    println("Failed to update product image node for id: $id")
                }
            } else {
                println("Error creating new product image id: ${keyResponse.status.value} - ${keyResponse.bodyAsText()}")
            }
        } catch (e: Exception) {
            println("Exception in createNewProductImageId: ${e.message}")
            e.printStackTrace()
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
