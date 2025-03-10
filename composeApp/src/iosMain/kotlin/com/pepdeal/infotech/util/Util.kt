package com.pepdeal.infotech.util

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*
import kotlin.math.round
import kotlinx.datetime.Clock
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.ToastPadding
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
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
            if (productSale.value.text != "0.00") {
                productSale.value = TextFieldValue("0.00")
                onCallBack() // Call callback only when crossing the 100 limit
            }
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
            // Remove +91 if present at the beginning
            val formattedPhoneNo = phoneNo.replace(Regex("^\\+91"), "")

            val urlString = "tel:$formattedPhoneNo" // ✅ Correct format
            val url = NSURL(string = urlString)

            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>()) { success ->
                    if (!success) {
                        println("Failed to open dialer for: $urlString")
                    }
                }
            } else {
                println("Invalid phone number format: $urlString")
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

    fun showToast(message:String,duration: ToastDuration = ToastDuration.Short){
      multiplatform.network.cmptoast.showToast(
            message = message,
            textColor = Color.White,
            duration = duration,
            gravity = ToastGravity.Bottom,
          backgroundColor = Color.DarkGray,
          padding = ToastPadding(10,10,20,20),
          cornerRadius = 3,
          textSize = TextUnit(13F, TextUnitType.Sp)
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    fun showCompleteToast(message: String) {
        val window = UIApplication.sharedApplication.keyWindow ?: return

        // Get screen width and height
        val screenWidth = window.bounds.useContents { size.width }
        val screenHeight = window.bounds.useContents { size.height }

        // Create Toast View
        val toastView = UILabel().apply {
            text = message
            textAlignment = NSTextAlignmentCenter
            backgroundColor = UIColor(red = 102.0/255.0, green = 153.0/255.0, blue = 0.0, alpha = 1.0)
            layer.cornerRadius = 6.0  // ✅ Rounded corners
            layer.shadowOpacity = 0.3f
            layer.shadowOffset = CGSizeMake(0.0, 4.0)
            layer.shadowRadius = 10.0
            clipsToBounds = false
            textColor = UIColor.whiteColor
            setFrame(CGRectMake(25.0, screenHeight - 120, screenWidth - 50.0, 50.0)) // ✅ Adjusted margins
        }

      // Create Label for Message
//        val textLabel = UILabel().apply {
//            text = message
//            textAlignment = NSTextAlignmentCenter
//            textColor = UIColor.whiteColor
//            font = UIFont.boldSystemFontOfSize(18.0)
//            numberOfLines = 2
//            sizeToFit() // Auto-size based on text
//            val textHeight = this.bounds.useContents { size.height } + 20.0 // Add padding
//            setFrame(CGRectMake(0.0, 0.0, screenWidth - 50, textHeight)) // Fixed width, dynamic height
//        }

//        toastView.addSubview(textLabel)
        window.addSubview(toastView)

        // Animate Fade-In and Fade-Out
        toastView.alpha = 0.0
        UIView.animateWithDuration(0.3, animations = {
            toastView.alpha = 1.0
        }) {
            NSTimer.scheduledTimerWithTimeInterval(2.0, repeats = false) {
                UIView.animateWithDuration(0.5, animations = {
                    toastView.alpha = 0.0
                }) {
                    toastView.removeFromSuperview()
                }
            }
        }
    }
    private const val TERM_CONDITION_URL = "https://www.termsfeed.com/live/b4f5f31b-3775-4a13-b785-f17154417fe6"
    fun openUrlInBrowser() {
        val nsUrl = NSURL.URLWithString(TERM_CONDITION_URL)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>()) { success ->
                if (!success) {
                    println("Failed to open URL: $TERM_CONDITION_URL")
                }
            }
        } else {
            println("Invalid URL: $TERM_CONDITION_URL")
        }
    }

}