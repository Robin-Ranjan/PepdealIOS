package com.pepdeal.infotech

import androidx.compose.ui.graphics.ImageBitmap
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.user.repository.PersonalInfoRepo
import com.pepdeal.infotech.user.repository.PersonalInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileScreenViewModal(
    private val prefRepo: PreferencesRepository,
    private val personalRepo: PersonalInfoRepository
) : ViewModel() {
    private val repo = PersonalInfoRepo()
    private val datastore = DataStore.dataStore
    private val _state = MutableStateFlow(ProfileScreenUi())
    val state = _state.asStateFlow()
        .onStart { observerUser() }
        .stateIn(
            viewModelScope,
            initialValue = _state.value,
            started = SharingStarted.WhileSubscribed(5000)
        )

    init {
        viewModelScope.launch {
            personalRepo.fetchUserProfilePic("").collect {

            }
        }
    }

    private val _userProfilePicMaster = MutableStateFlow<UserProfilePicMaster?>(null)
    val userProfilePicMaster: StateFlow<UserProfilePicMaster?> get() = _userProfilePicMaster

    private var hasFetchedProfilePic = false // Prevents repeated API calls

    private fun observerUser() {
        viewModelScope.launch {
            println("User fetching ")
            val user = prefRepo.getDataClass(
                key = PreferencesKeys.user_data_key,
                serializer = UserMaster.serializer(),
            )
            println("User $user")
            _state.update { it.copy(user = user) }
        }
    }

    fun fetchUserProfilePic(userId: String) {
        if (hasFetchedProfilePic) return // Skip if already fetched

        viewModelScope.launch(Dispatchers.IO) {
            val userProfile = repo.fetchUserProfilePic(userId)
            println(userProfile)
            _userProfilePicMaster.value = userProfile
            hasFetchedProfilePic =
                userProfile?.profilePicUrl.isNullOrEmpty().not() // Fetch only once
        }
    }

    fun fetchShopId(shopMobileNo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            println("shopId fetching ")
            val shopId = repo.fetchShopId(shopMobileNo)
            if (shopId != null) {
                datastore.edit { pref ->
                    pref[PreferencesKeys.SHOPID_KEY] = shopId
                }
            }
        }
    }

    fun uploadNewProfilePic(userId: String, profilePicImage: ImageBitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.updateProfilePic(userId, profilePicImage)
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    fun reset() {
        _userProfilePicMaster.value = null
        hasFetchedProfilePic = false
    }
}

data class ProfileScreenUi(
    val user: UserMaster? = null,
    val userProfilePicMaster: UserProfilePicMaster? = null,
    val hasFetchedProfilePic: Boolean = false,
) {
    val isLogin: Boolean = user != null
}