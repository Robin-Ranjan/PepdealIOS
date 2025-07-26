package com.pepdeal.infotech.user.repository

import com.pepdeal.infotech.UserProfilePicMaster
import kotlinx.coroutines.flow.Flow

interface PersonalInfoRepository {
    suspend fun fetchUserProfilePic(userId: String): Flow<Result<UserProfilePicMaster>>
}