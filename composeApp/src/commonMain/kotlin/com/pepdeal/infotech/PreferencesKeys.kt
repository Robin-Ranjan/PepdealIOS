package com.pepdeal.infotech

import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val USERID_KEY =  stringPreferencesKey("user_id")
    val USER_NAME =  stringPreferencesKey("user_name")
    val SHOPID_KEY =  stringPreferencesKey("shop_id")
    val MOBILE_NO = stringPreferencesKey("mobile_no")
    val USER_STATUS = stringPreferencesKey("user_status")
}