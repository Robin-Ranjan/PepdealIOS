package com.pepdeal.infotech

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.pepdeal.infotech.util.ImagesUtil.toUIImage
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

class ImageCompressor {

    @OptIn(ExperimentalForeignApi::class)
    suspend fun compress(image: ImageBitmap, compressionThreshold: Long): ImageBitmap =
        withContext(Dispatchers.Default) {
            // Convert the Compose ImageBitmap to UIImage.
            // You must implement toUIImage() on ImageBitmap.
            val uiImage: UIImage = image.toUIImage() ?: error("Conversion to UIImage returned null")

            var quality = 1.0  // Quality from 0.0 to 1.0
            var compressedData: NSData? = null
            // Iteratively compress until below the threshold.
            do {
                compressedData = UIImageJPEGRepresentation(uiImage, quality)
                quality -= 0.1
            } while ((compressedData?.length?.toLong() ?: Long.MAX_VALUE) > compressionThreshold && quality > 0.1)

            // Calculate the size in MB.
            val sizeInBytes = compressedData?.length?.toLong() ?: 0L
            val sizeInMB = sizeInBytes.toDouble() / 1024.0
            println("Compressed image size: $sizeInMB kB")


            // Create a UIImage from compressed data.
            val compressedUIImage = compressedData?.let { UIImage.imageWithData(it) }
            // Convert the compressed UIImage to an ImageBitmap.
            compressedUIImage?.toImageBitmap() ?: error("Compression failed")
        }

    // Convert NSData to ByteArray.
    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val byteArray = ByteArray(length)
        memScoped {
            val bytes = this@toByteArray.bytes?.reinterpret<ByteVar>()
                ?: error("NSData.bytes is null")
            for (i in 0 until length) {
                byteArray[i] = bytes[i]
            }
        }
        return byteArray
    }

    // Convert UIImage to Compose ImageBitmap.
    private fun UIImage.toImageBitmap(): ImageBitmap {
        // Get NSData from UIImage (using highest quality, 1.0).
        val data: NSData = UIImageJPEGRepresentation(this, 1.0)
            ?: error("Unable to get NSData from UIImage")
        val byteArray = data.toByteArray()
        // Create a Skia Image from the encoded bytes.
        val skiaImage = Image.makeFromEncoded(byteArray)
        return skiaImage.toComposeImageBitmap()
    }
}