package com.pepdeal.infotech.shopVideo

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.*
import kotlin.math.roundToInt

class UploadShopVideoRepo {

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


}