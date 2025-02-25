package com.pepdeal.infotech.util

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateWithName
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.CoreGraphics.kCGColorSpaceSRGB
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Foundation.*
import platform.posix.*

object ImagesUtil {

    fun UIImage.toNSData(): NSData? {
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

        // Use sRGB color space
        val colorSpace = CGColorSpaceCreateWithName(kCGColorSpaceSRGB)
        val bytesPerRow = 4 * width
        val rawBuffer = buffer.usePinned { pinned ->
            CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = bytesPerRow.toULong(),
                space = colorSpace,
                // Changed bitmapInfo: Use premultiplied first with little-endian byte order
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or kCGBitmapByteOrder32Little
            )
        }

        val cgImage = rawBuffer?.let { CGBitmapContextCreateImage(it) }
        return cgImage?.let { UIImage.imageWithCGImage(it) }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun readFileAsByteArray(filePath: String): ByteArray? {
        val nsUrl = NSURL.fileURLWithPath(filePath)
        return try {
            val data = NSData.dataWithContentsOfURL(nsUrl)
            data?.let { it.bytes?.readBytes(it.length.toInt()) }
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            null
        }
    }

}