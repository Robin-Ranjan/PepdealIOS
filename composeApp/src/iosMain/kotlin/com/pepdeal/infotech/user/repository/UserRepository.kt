package com.pepdeal.infotech.user.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.user.UserMaster

interface UserRepository {
    suspend fun fetchUserDetails(userId: String): AppResult<UserMaster, DataError.Remote>
}