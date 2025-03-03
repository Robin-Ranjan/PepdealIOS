package com.pepdeal.infotech.user

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.UserProfilePicMaster
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

    private val _userProfilePicMaster = MutableStateFlow<UserProfilePicMaster?>(null)
    val userProfilePicMaster : StateFlow<UserProfilePicMaster?> get() = _userProfilePicMaster


    private var hasFetchedProfilePic = false

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

    fun fetchUserProfilePic(userId:String){
        if (hasFetchedProfilePic) return // Skip if already fetched

        viewModelScope.launch(Dispatchers.IO) {
            val userProfile = repo.fetchUserProfilePic(userId)
            println(userProfile)
            _userProfilePicMaster.value = userProfile
            hasFetchedProfilePic = userProfile?.profilePicUrl.isNullOrEmpty().not() // Fetch only once
        }
    }

    fun uploadNewProfilePic(userId: String,profilePicImage: ImageBitmap){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.uploadThumbnailWithDelete(userId, profilePicImage)
            } catch (e:Exception){
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    fun reset(){
        _userDetails.value = UserMaster()
        _userProfilePicMaster.value = null
    }
}