package com.pepdeal.infotech.login.viewModel

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pepdeal.infotech.DataStore
import com.pepdeal.infotech.PreferencesKeys
import com.pepdeal.infotech.core.base_ui.SnackBarMessage
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.dataStore.PreferencesRepository
import com.pepdeal.infotech.login.repository.LoginRepository
import com.pepdeal.infotech.navigation.routes.Routes
import com.pepdeal.infotech.user.UserMaster
import com.pepdeal.infotech.util.NavigationProvider
import com.pepdeal.infotech.util.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModal(
    private val loginRepo: LoginRepository,
    private val prefRepo: PreferencesRepository
) : ViewModel() {
    private val datastore = DataStore.dataStore
    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _state.value
        )

    fun onAction(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.OnMobileNoChange -> {
                _state.update { it.copy(mobileNo = action.mobileNo) }
            }

            is LoginUiAction.OnPassChange -> {
                _state.update { it.copy(password = action.pass) }
            }

            is LoginUiAction.OnSubmitClick -> {
                if (_state.value.isFormValid) {
                    validateUser(
                        "+91${_state.value.mobileNo}",
                        Util.hashPassword(_state.value.password)
                    )
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            snackBarMessage = SnackBarMessage.Error(message = _state.value.getError())
                        )
                    }
                }

            }

            LoginUiAction.OnClearSnackBar -> {
                _state.update { it.copy(snackBarMessage = null) }
            }

        }
    }

    private fun validateUser(mobileNo: String, pass: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val userResult = fetchUser(mobileNo, pass)) {
                is AppResult.Success -> {
                    val user = userResult.data
                    if (user == null) {
                        emitError("User not found.")
                        return@launch
                    }

                    val saveResult = saveUserLocally(user)
                    if (saveResult is AppResult.Success) {
                        navigateToMain(user)
                    } else {
                        emitError("Failed to save user data.")
                    }
                }

                is AppResult.Error -> {
                    val message = when (userResult.error.type) {
                        DataError.RemoteType.PASS_INCORRECT -> userResult.error.message
                            ?: "Incorrect password."

                        else -> userResult.error.message
                            ?: "Something went wrong. Please try again."
                    }
                    emitError(message)
                }
            }
        }
    }

    private suspend fun fetchUser(
        mobileNo: String,
        pass: String
    ): AppResult<UserMaster?, DataError.Remote> {
        return try {
            var userResult: AppResult<UserMaster?, DataError.Remote>? = null
            loginRepo.validateUserLogin(mobileNo, pass).collect { result ->
                userResult = result
            }
            userResult ?: AppResult.Error(
                DataError.Remote(
                    DataError.RemoteType.SERVER,
                    "Empty response from server"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Remote(
                    DataError.RemoteType.SERVER,
                    e.message ?: "Exception occurred during login"
                )
            )
        }
    }

    private suspend fun saveUserLocally(user: UserMaster): AppResult<Unit, DataError.Local> {
        return try {
            var saveResult: AppResult<Unit, DataError.Local>? = null

            prefRepo.saveDataClass(
                key = PreferencesKeys.user_data_key,
                data = user,
                serializer = UserMaster.serializer()
            ).collect { result ->
                saveResult = result
            }

            if (saveResult is AppResult.Success) {
                prefRepo.saveString(PreferencesKeys.USERID_KEY, user.userId)
                prefRepo.saveString(PreferencesKeys.MOBILE_NO, user.mobileNo)
                prefRepo.saveString(PreferencesKeys.USER_STATUS, user.userStatus)
                prefRepo.saveString(PreferencesKeys.USER_NAME, user.userName)

                datastore.edit { pref->
                    pref[PreferencesKeys.USERID_KEY] = user.userId
                    pref[PreferencesKeys.MOBILE_NO] = user.mobileNo
                    pref[PreferencesKeys.USER_STATUS] = user.userStatus
                    pref[PreferencesKeys.USER_NAME] = user.userName
                }
            }

            saveResult ?: AppResult.Error(
                DataError.Local(DataError.LocalType.UNKNOWN, "Empty result while saving user.")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(
                DataError.Local(DataError.LocalType.UNKNOWN, e.message ?: "Failed to save user.")
            )
        }
    }

    private fun navigateToMain(user: UserMaster) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    userMaster = user,
                    isLoading = false,
                    loginStatus = true,
                    snackBarMessage = SnackBarMessage.Success("Login Successful")
                )
            }

            delay(500)

            NavigationProvider.navController.navigate(Routes.MainPage) {
                popUpTo<Routes.LoginPage> { inclusive = true }
            }
        }
    }

    private fun emitError(message: String) {
        _state.update {
            it.copy(
                isLoading = false,
                snackBarMessage = SnackBarMessage.Error(message)
            )
        }
    }
}

data class LoginUiState(
    val userMaster: UserMaster? = null,
    val mobileNo: String = "",
    val password: String = "",
    val loginStatus: Boolean = false,
    val isLoading: Boolean = false,
    val snackBarMessage: SnackBarMessage? = null
) {
    val isFormValid: Boolean = mobileNo.length == 10 && password.isNotBlank()
    fun getError(): String =
        when {
            mobileNo.isBlank() -> "Mobile number cannot be empty"
            mobileNo.length != 10 -> "Mobile number must be 10 digits"
            password.isBlank() -> "Password cannot be empty"
            else -> ""
        }
}

sealed interface LoginUiAction {
    data class OnMobileNoChange(val mobileNo: String) : LoginUiAction
    data class OnPassChange(val pass: String) : LoginUiAction
    data object OnSubmitClick : LoginUiAction
    data object OnClearSnackBar : LoginUiAction
}