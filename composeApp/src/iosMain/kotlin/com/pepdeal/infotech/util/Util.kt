package com.pepdeal.infotech.util

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*
import kotlin.math.round
import kotlinx.datetime.Clock
import platform.UIKit.*


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
        } catch (e: Exception) {
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
            if (hex.isBlank() || hex.isEmpty()) return Black
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
        } catch (e: Exception) {
            e.printStackTrace()
            println("${e.message} and $hex")
            return Black
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

    fun calculateFinalPrice(
        mrpText: String,
        discountText: String,
        productSale: MutableState<TextFieldValue>,
        onCallBack: () -> Unit
    ) {
        val price = mrpText.toDoubleOrNull() ?: 0.0
        val discount = discountText.toDoubleOrNull() ?: 0.0

        if (discount > 100) {
            productSale.value = TextFieldValue("0.00")
            onCallBack()
            return
        } // Reset if discount is greater than 100

        val finalPrice = price - (price * discount / 100)

        // Round to 2 decimal places
        val roundedFinalPrice = (round(finalPrice * 100) / 100).toString()

        productSale.value = TextFieldValue(roundedFinalPrice)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun hashPassword(password: String): String {
        val data = password.encodeToByteArray().toUByteArray() // Convert to UByteArray
        val hash = UByteArray(CC_SHA256_DIGEST_LENGTH)

        data.usePinned { pinned ->
            hash.usePinned { hashPinned ->
                CC_SHA256(pinned.addressOf(0), data.size.convert(), hashPinned.addressOf(0))
            }
        }

        return hash.joinToString("") { it.toString(16).padStart(2, '0') } // Corrected
    }

    fun formatDateWithTimestamp(timeMillis: String): String {
        return try {
            val timestamp = timeMillis.toDoubleOrNull()?.div(1000) ?: return "" // Safely convert to Double and handle errors
            val date = NSDate.dateWithTimeIntervalSince1970(timestamp) // Create NSDate from timestamp

            val dateFormatter = NSDateFormatter().apply {
                dateFormat = "dd MMM yyyy"
                locale = NSLocale.currentLocale // Ensure it uses the correct locale
            }
            dateFormatter.stringFromDate(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun getCurrentTimeStamp(): String {
        return Clock.System.now().toEpochMilliseconds().toString()
    }

    fun setStatusBarColor(hexColor: String, isDark: Boolean) {
        val color = UIColor(
            red = hexColor.substring(1, 3).toInt(16) / 255.0,
            green = hexColor.substring(3, 5).toInt(16) / 255.0,
            blue = hexColor.substring(5, 7).toInt(16) / 255.0,
            alpha = 1.0
        )

        val window = UIApplication.sharedApplication.keyWindow
        window?.rootViewController?.view?.backgroundColor = color

        // Change the status bar text color
        val statusBarStyle = if (isDark) {
            UIStatusBarStyleDarkContent  // Dark text (for light backgrounds)
        } else {
            UIStatusBarStyleLightContent // White text (for dark backgrounds)
        }

        // ✅ Use a UIWindowScene method to change status bar style dynamically (iOS 13+)
        if (window != null) {
            window.overrideUserInterfaceStyle =
                if (isDark) UIUserInterfaceStyle.UIUserInterfaceStyleLight else UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
    }

    fun openDialer(phoneNo: String) {
        if (phoneNo != "-1") {
            // Remove +91 if it exists at the beginning
            val formattedPhoneNo = phoneNo.replace(Regex("^\\+91"), "")

            val urlString = "tel:$formattedPhoneNo"
            val url = NSURL(string = urlString)

            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            } else {
                println("Invalid URL: $urlString")
            }
        } else {
            println("Something went wrong")
        }
    }

    // Cross-platform email validation
    fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex() // Works on both iOS and Android
        return emailRegex.matches(email)
    }

}