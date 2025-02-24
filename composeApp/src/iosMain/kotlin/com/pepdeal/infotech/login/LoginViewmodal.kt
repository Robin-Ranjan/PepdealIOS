package com.pepdeal.infotech.login

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.PrefsDataStore
import com.pepdeal.infotech.product.ShopItems
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModal() :ViewModel(){
    private val datastore = DataStore.dataStore

    private val loginRepo = LoginRepo()
//    private val _products =
//        MutableStateFlow<List<ShopItems>>(emptyList()) // StateFlow to hold product data
//    val products: StateFlow<List<ShopItems>> get() = _products.asStateFlow()

    private val _loginStatus = MutableStateFlow(false)
    val loginStatus: StateFlow<Boolean> get() = _loginStatus.asStateFlow()

    private val _loginMessage = MutableStateFlow("")
    val loginMessage: StateFlow<String> get() = _loginMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading :StateFlow<Boolean> get() = _isLoading

     fun validateUser(mobileNo: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            loginRepo.validateUserLogin(mobileNo, pass) { success, message,userId,userStatus , userName->
                if(success){
                    viewModelScope.launch {
                        datastore.edit { pref->
                            pref[PreferencesKeys.USERID_KEY] = userId
                            pref[PreferencesKeys.MOBILE_NO] = mobileNo
                            pref[PreferencesKeys.USER_STATUS] = userStatus
                            pref[PreferencesKeys.USER_NAME] = userName
                        }
                    }
                }
                _loginStatus.value = success
                _loginMessage.value = message
                _isLoading.value =false
            }
        }
    }

    fun reset(){
        _loginStatus.value = false
        _loginMessage.value = ""
    }
}