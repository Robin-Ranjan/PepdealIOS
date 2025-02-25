package com.pepdeal.infotech.util

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceCreateWithName
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.kCGColorSpaceSRGB
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

object ImagesUtil {

    fun UIImage.toNSData(): NSData? {
        return UIImageJPEGRepresentation(this, 0.8)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        return (this.bytes?.readBytes(this.length.toInt())) ?: ByteArray(0)
    }

//    @OptIn(ExperimentalForeignApi::class)
//    fun ImageBitmap.toUIImage(): UIImage? {
//        val width = this.width
//        val height = this.height
//        val buffer = IntArray(width * height)
//
//        this.readPixels(buffer)
//
//        val colorSpace = CGColorSpaceCreateDeviceRGB()
//        val context = CGBitmapContextCreate(
//            data = buffer.refTo(0),
//            width = width.toULong(),
//            height = height.toULong(),
//            bitsPerComponent = 8u,
//            bytesPerRow = (4 * width).toULong(),
//            space = colorSpace,
//            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
//        )
//        val cgImage = CGBitmapContextCreateImage(context)
//        return cgImage?.let { UIImage.imageWithCGImage(it) }
//    }

    @OptIn(ExperimentalForeignApi::class)
    fun ImageBitmap.toUIImage(): UIImage? {
        val width = this.width
        val height = this.height
        val buffer = IntArray(width * height)

        this.readPixels(buffer)

//        val colorSpace = CGColorSpaceCreateDeviceRGB()
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
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaNoneSkipLast.value
            )
        }

        val cgImage = rawBuffer?.let { CGBitmapContextCreateImage(it) }
        return cgImage?.let { UIImage.imageWithCGImage(it) }
    }

}