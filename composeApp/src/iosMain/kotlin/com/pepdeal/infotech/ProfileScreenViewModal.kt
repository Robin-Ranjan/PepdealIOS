package com.pepdeal.infotech

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.user.PersonalInfoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileScreenViewModal():ViewModel() {
    private val repo = PersonalInfoRepo()
    private val datastore = DataStore.dataStore

    private val _userProfilePicMaster = MutableStateFlow<UserProfilePicMaster?>(null)
    val userProfilePicMaster : StateFlow<UserProfilePicMaster?> get() = _userProfilePicMaster

    private var hasFetchedProfilePic = false // Prevents repeated API calls

    fun fetchUserProfilePic(userId:String){
        if (hasFetchedProfilePic) return // Skip if already fetched

        viewModelScope.launch {
            val userProfile = repo.fetchUserProfilePic(userId)
            println(userProfile)
            _userProfilePicMaster.value = userProfile
            hasFetchedProfilePic = userProfile?.profilePicUrl.isNullOrEmpty().not() // Fetch only once
        }
    }

    fun fetchShopId(shopMobileNo:String){
        viewModelScope.launch {
            val shopId = repo.fetchShopId(shopMobileNo)
            if(shopId!=null){
                datastore.edit { pref->
                    pref[PreferencesKeys.SHOPID_KEY] = shopId
                }
            }
        }
    }
    fun reset(){
        _userProfilePicMaster.value = null
        hasFetchedProfilePic = false
    }
}