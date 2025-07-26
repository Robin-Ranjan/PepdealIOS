package com.pepdeal.infotech.login.repository

import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.user.UserMaster
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    suspend fun validateUserLogin(
        mobileNo: String,
        pass: String,
    ): Flow<AppResult<UserMaster, DataError.Remote>>
}