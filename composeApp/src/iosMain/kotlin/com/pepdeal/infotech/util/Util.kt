package com.pepdeal.infotech.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.UIKit.UIColor

object Util {
    fun String.toRupee(): String = "₹$this"

    fun String.toNameFormat(): String {
        return this.split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
//                    Locale.Root
                ) else it.toString()
            }
        }
    }

    fun String.formatPhoneNumber(): String {
        return if (this.startsWith("+91") && this.length >= 12 && !this.contains("-")) {
            "+91-${this.substring(3)}"
        } else {
            "+91-$this"
        }
    }

    fun String.removePrefixFromPhoneNumber(): String {
        return if (this.startsWith("+91") && this.length >= 12) {
            this.substring(3)
        } else {
            this
        }
    }

    fun String.toDiscountFormat(): String {
        return try {
            "-$this%"
        }catch (e:Exception){
            e.printStackTrace()
            Firebase.crashlytics.recordException(e)
            println("DiscountFormat toDiscountFormat: ${e.message}")
            this
        }
    }

    fun String.toTwoDecimalPlaces(): String {
        return try {
            val formatter = NSNumberFormatter()
            formatter.minimumFractionDigits = 2u
            formatter.maximumFractionDigits = 2u

            // Convert the string to a Double and create an NSNumber
            val number = this.toDoubleOrNull() ?: return this
            val nsNumber = NSNumber(number)

            // Format the number
            formatter.stringFromNumber(nsNumber) ?: this
        } catch (e: Exception) {
            this
        }
    }

    fun Color.Companion.fromHex(hex: String): Color {
        try {
            // Remove the '#' character if it's there
            val cleanedHex = hex.removePrefix("#")

            // Ensure the hex code is valid (length 6 for RGB, 8 for ARGB)
            require(cleanedHex.length == 6 || cleanedHex.length == 8) { "Invalid hex color code" }

            // Parse the color components (Red, Green, Blue, and optionally Alpha)
            val r = cleanedHex.substring(0, 2).toInt(16)
            val g = cleanedHex.substring(2, 4).toInt(16)
            val b = cleanedHex.substring(4, 6).toInt(16)

            val a = if (cleanedHex.length == 8) {
                cleanedHex.substring(6, 8).toInt(16)
            } else {
                255 // Default alpha value if not provided
            }

            // Return the Color object using integer values (0 to 255)
            return Color(r, g, b, a)
        }catch (e:Exception){
            e.printStackTrace()
            println(e.message)
            return Color.Unspecified
        }
    }

    fun validateShopAndSubmit(
        fields: Map<String, String>,
        setError: (String) -> Unit,
        status: (Boolean) -> Unit
    ) {
        // Check for empty fields
        val emptyField = fields.entries.find { it.value.isBlank() }
        if (emptyField != null) {
            setError("${emptyField.key} cannot be empty!")
            status(false)
        } else {
            // Validate phone number length
            val phoneNumber = fields["Shop Phone Number"]
            if (phoneNumber != null && phoneNumber.length < 10) {
                setError("Phone number must be at least 10 digits long!")
                status(false)
                return
            }

            // Clear any previous error and submit logic
            setError("")
            // Submit logic
            println("Form submitted successfully")
            status(true)
        }
    }
}