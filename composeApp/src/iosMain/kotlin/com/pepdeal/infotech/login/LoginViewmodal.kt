package com.pepdeal.infotech.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.product.ShopItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModal() :ViewModel(){

    private val loginRepo = LoginRepo()
    private val _products =
        MutableStateFlow<List<ShopItems>>(emptyList()) // StateFlow to hold product data
    val products: StateFlow<List<ShopItems>> get() = _products

    private val _loginStatus = MutableStateFlow(false)
    val loginStatus: StateFlow<Boolean> get() = _loginStatus

    private val _loginMessage = MutableStateFlow("")
    val loginMessage: StateFlow<String> get() = _loginMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading :StateFlow<Boolean> get() = _isLoading

    fun validateUser(mobileNo: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            loginRepo.validateUserLogin(mobileNo, pass) { success, message ->
                _loginStatus.value = success
                _loginMessage.value = message
                _isLoading.value =false
            }
        }
    }
}