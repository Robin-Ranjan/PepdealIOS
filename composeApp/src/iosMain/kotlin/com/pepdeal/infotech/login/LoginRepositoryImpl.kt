package com.pepdeal.infotech.login

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.login.repository.LoginRepository
import com.pepdeal.infotech.user.UserMaster
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginRepositoryImpl(private val httpClient: HttpClient) : LoginRepository {

    override suspend fun validateUserLogin(
        mobileNo: String,
        pass: String,
    ): Flow<AppResult<UserMaster, DataError.Remote>> = flow {

        try {
            val queryBody = buildFirestoreQuery(
                collection = DatabaseCollection.USER_MASTER,
                filters = listOf(
                    FirestoreFilter("mobileNo", mobileNo)
                ),
                limit = 1
            )

            val userResponse: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
                httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(queryBody)
                }
            }

            when (userResponse) {
                is AppResult.Error -> {
                    emit(
                        AppResult.Error(
                            DataError.Remote(
                                type = DataError.RemoteType.NOT_FOUND,
                                message = "User Not Found"
                            )
                        )
                    )
                    return@flow
                }

                is AppResult.Success -> {
                    val user = userResponse.data.firstOrNull()?.document?.fields?.let { fields ->
                        UserMaster(
                            userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userName = (fields["userName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            mobileNo = (fields["mobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            emailId = (fields["emailId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            password = (fields["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            fcmToken = (fields["fcmToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            deviceToken = (fields["fcmToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            userStatus = (fields["userStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                    }
                    user?.let {
                        if (it.password == pass) {
                            emit(AppResult.Success(it))
                        } else {
                            emit(
                                AppResult.Error(
                                    DataError.Remote(
                                        type = DataError.RemoteType.PASS_INCORRECT,
                                        message = "Incorrect Password"
                                    )
                                )
                            )
                        }
                    } ?: run {
                        emit(
                            AppResult.Error(
                                DataError.Remote(
                                    type = DataError.RemoteType.SERVER,
                                    message = "Something Went Wrong , try again"
                                )
                            )
                        )
                    }

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
            AppResult.Error(
                DataError.Remote(
                    type = DataError.RemoteType.SERVER,
                    message = "Something Went Wrong , try again"
                )
            )
        }
    }
}