package com.pepdeal.infotech.user

import UserMaster
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PersonalInfoViewModal() : ViewModel() {
    private val repo = PersonalInfoRepo()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> get() = _uploading

    private val _userDetails = MutableStateFlow(UserMaster())
    val userDetails: StateFlow<UserMaster> get() = _userDetails

    fun fetchUserDetails(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val userDetails = repo.fetchUserDetails2(userId)
            _userDetails.value = userDetails
            _isLoading.value = false
        }
    }

    fun updateUserEmailId(userId: String, emailId: String, onSuccess: (Boolean) -> Unit) {
        _uploading.value = true
        viewModelScope.launch {
            val success = repo.updateUserEmailId(userId, emailId) // Now calling suspend function inside coroutine
            _uploading.value = false
            withContext(Dispatchers.Main) { onSuccess(success) } // Ensure UI update on main thread
        }

    }

    fun reset(){
        _userDetails.value = UserMaster()
    }
}