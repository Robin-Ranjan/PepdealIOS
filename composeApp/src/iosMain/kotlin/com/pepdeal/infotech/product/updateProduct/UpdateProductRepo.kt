package com.pepdeal.infotech.product.updateProduct

import androidx.compose.ui.graphics.ImageBitmap
import com.pepdeal.infotech.FirebaseUploadResponse
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.product.ProductImageMaster
import com.pepdeal.infotech.product.ProductMaster
import com.pepdeal.infotech.product.ProductWithImages
import com.pepdeal.infotech.product.producrDetails.ProductDetailsRepo
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
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UpdateProductRepo {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetchProductDetails(productId: String): ProductWithImages? {
        val productDetails: ProductWithImages?
        try {
            productDetails = ProductDetailsRepo().fetchTheProductDetails(productId,
                filterList = listOf(FirestoreFilter("productId", productId))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            return null
        }
        return productDetails
    }

    // Main function to update product details and replace images
    suspend fun updateProductWithImages(
        productId: String,
        updatedProductMaster: ProductMaster,
        isImageEdited: Boolean,
        newUriList: MutableList<ImageBitmap>,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            val bucket = FirebaseUtil.BUCKET_URL

            val updateMap: Map<String, String> = mapOf(
                "productName" to updatedProductMaster.productName,
                "brandName" to updatedProductMaster.brandName,
                "categoryId" to updatedProductMaster.categoryId,
                "subCategoryId" to updatedProductMaster.subCategoryId,
                "description" to updatedProductMaster.description,
                "description2" to updatedProductMaster.description2,
                "specification" to updatedProductMaster.specification,
                "warranty" to updatedProductMaster.warranty,
                "color" to updatedProductMaster.color,
                "mrp" to updatedProductMaster.mrp,
//                "searchTag" to updatedProductMaster.searchTag,
                "onCall" to updatedProductMaster.onCall,
                "discountMrp" to updatedProductMaster.discountMrp,
                "sellingPrice" to updatedProductMaster.sellingPrice,
                "sizeName" to updatedProductMaster.sizeName,
                "updatedAt" to updatedProductMaster.updatedAt,
                "isActive" to updatedProductMaster.productActive,
                "flag" to updatedProductMaster.flag
            )

            val response: HttpResponse =
                client.get("${FirebaseUtil.BASE_URL}product_master.json?orderBy=\"productId\"&equalTo=\"$productId\"") {
                    contentType(ContentType.Application.Json)
                }

            val responseBody = response.bodyAsText()
            println("response Body :_ $responseBody")
            if (responseBody.isEmpty() || responseBody == "null") {
                onComplete(false, "Product not found")
            }

            // Parse the response to extract the node key.
            val nodeMap: Map<String, ProductMaster> = json.decodeFromString(responseBody)
            val nodeKey = nodeMap.keys.firstOrNull()
            if (nodeKey == null) {
                onComplete(false, "Product not found")
            }

            // Step 2: Update the product using the node key.
            val updateUrl = "${FirebaseUtil.BASE_URL}product_master/$nodeKey.json"
            val jsonBody = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                updateMap
            )
            val patchResponse: HttpResponse = client.patch(updateUrl) {
                contentType(ContentType.Application.Json)
                setBody(TextContent(jsonBody, ContentType.Application.Json))
            }

            if (!patchResponse.status.isSuccess()) {
                onComplete(false, "Failed to update product details: ${patchResponse.status}")
            } else {
                if (isImageEdited) {
                    deleteAllImages(bucket = bucket, productId)

                    // Step 4: Upload Images and collect overall status and message.
                    val (imagesUploaded, imageMsg) = uploadImagesToFirebase(productId, newUriList)

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
                } else {
                    onComplete(true, "Product is Updated.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            onComplete(false, e.message.toString())
        }

    }

    private suspend fun deleteAllImages(
        bucket: String,
        productId: String
    ) {
        val client = HttpClient(Darwin)

        try {
            // Step 1: List all images under `fileName`
            val listUrl =
                "https://firebasestorage.googleapis.com/v0/b/$bucket/o?prefix=product_images/$productId/"
            val listResponse: HttpResponse = client.get(listUrl)

            if (listResponse.status.isSuccess()) {
                val responseText = listResponse.bodyAsText()
                val json = Json.parseToJsonElement(responseText).jsonObject
                val items = json["items"]?.jsonArray ?: return

                // Step 2: Loop through each file and delete
                for (item in items) {
                    val fullPath = item.jsonObject["name"]?.jsonPrimitive?.content ?: continue
                    val encodedPath = fullPath.replace("/", "%2F") // Ensure proper encoding

                    val deleteUrl =
                        "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath"

                    val deleteResponse: HttpResponse = client.request(deleteUrl) {
                        method = HttpMethod.Delete
                    }

                    if (deleteResponse.status.isSuccess()) {
                        println("Deleted: $fullPath")
                    } else {
                        println("Failed to delete: $fullPath, Status: ${deleteResponse.status}")
                    }
                }
            } else {
                println("Failed to list images. Status: ${listResponse.status}")
            }

            // Step 3: Delete image URLs from Realtime Database
            val dbUrl =
                "${FirebaseUtil.BASE_URL}product_images_master.json?orderBy=\"productId\"&equalTo=\"$productId\""
            val dbResponse: HttpResponse = client.get(dbUrl)

            if (dbResponse.status.isSuccess()) {
                val dbText = dbResponse.bodyAsText()
                val dbJson = Json.parseToJsonElement(dbText).jsonObject

                for (key in dbJson.keys) {
                    val deleteDbUrl = "${FirebaseUtil.BASE_URL}product_images_master/$key.json"
                    val deleteDbResponse: HttpResponse = client.request(deleteDbUrl) {
                        method = HttpMethod.Delete
                    }

                    if (deleteDbResponse.status.isSuccess()) {
                        println("Deleted image record: $key from product_images_master")
                    } else {
                        println("Failed to delete image record: $key, Status: ${deleteDbResponse.status}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error during deletion: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    // STEP 2: Revised uploadImagesToFirebase
// Changed from using an onComplete callback to returning a Pair<Boolean, String> for overall result.
// ------------------------------
    private suspend fun uploadImagesToFirebase(
        productId: String,
        images: List<ImageBitmap>
    ): Pair<Boolean, String> {
        val bucket = FirebaseUtil.BUCKET_URL
        val baseFileName = "product_images/${productId}/image"

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
            return "https://firebasestorage.googleapis.com/v0/b/pepdeal-1251f.appspot.com/o/product_images%2F$productId%2F$imageName.jpg?alt=media&token=$token"
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
}