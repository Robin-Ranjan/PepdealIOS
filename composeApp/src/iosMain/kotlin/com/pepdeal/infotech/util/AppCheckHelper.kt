package com.pepdeal.infotech.util
import platform.Foundation.NSError
import platform.darwin.*
import kotlinx.cinterop.*

object AppCheckHelper {
    fun getAppCheckToken(callback: (String?) -> Unit) {
        AppCheckHelper.getAppCheckToken { token ->
            callback(token)
        }
    }
}