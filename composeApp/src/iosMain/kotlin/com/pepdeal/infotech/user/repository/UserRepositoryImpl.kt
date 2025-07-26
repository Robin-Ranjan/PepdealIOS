package com.pepdeal.infotech.user.repository

import com.pepdeal.infotech.core.data.safeCall
import com.pepdeal.infotech.core.databaseUtils.DatabaseCollection
import com.pepdeal.infotech.core.databaseUtils.DatabaseQueryResponse
import com.pepdeal.infotech.core.databaseUtils.DatabaseUtil
import com.pepdeal.infotech.core.databaseUtils.DatabaseValue
import com.pepdeal.infotech.core.databaseUtils.FirestoreFilter
import com.pepdeal.infotech.core.databaseUtils.buildFirestoreQuery
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.user.UserMaster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserRepositoryImpl(private val httpClient: HttpClient) : UserRepository {

    override suspend fun fetchUserDetails(userId: String): AppResult<UserMaster, DataError.Remote> {
        val queryBody = buildFirestoreQuery(
            collection = DatabaseCollection.USER_MASTER,
            limit = 1,
            filters = listOf(
                FirestoreFilter("userId", userId)
            )
        )

        val response: AppResult<List<DatabaseQueryResponse>, DataError.Remote> = safeCall {
            httpClient.post(DatabaseUtil.DATABASE_QUERY_URL) {
                contentType(ContentType.Application.Json)
                setBody(queryBody)
            }.body()
        }

        return when (response) {
            is AppResult.Error -> AppResult.Error(response.error)

            is AppResult.Success -> {
                val user = response.data.firstOrNull()?.document?.fields?.let { fields ->
                    UserMaster(
                        userId = (fields["userId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userName = (fields["userName"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        mobileNo = (fields["mobileNo"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        emailId = (fields["emailId"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        password = (fields["password"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        fcmToken = (fields["fcmToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        isActive = (fields["isActive"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        userStatus = (fields["userStatus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        deviceToken = (fields["deviceToken"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()

                    )
                } ?: UserMaster()
                AppResult.Success(user)
            }
        }
    }
}