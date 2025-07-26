package com.pepdeal.infotech.core.data

import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.domain.AppResult
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse
): AppResult<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (e: SocketTimeoutException) {
        return AppResult.Error(
            DataError.Remote(
                type = DataError.RemoteType.REQUEST_TIMEOUT,
                message = e.message
            )
        )
    } catch (e: UnresolvedAddressException) {
        return AppResult.Error(
            DataError.Remote(
                type = DataError.RemoteType.NO_INTERNET,
                message = e.message
            )
        )
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        return AppResult.Error(
            DataError.Remote(
                type = DataError.RemoteType.UNKNOWN,
                message = e.message
            )
        )
    }

    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): AppResult<T, DataError.Remote> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                AppResult.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERIALIZATION))
            }
        }

        408 -> AppResult.Error(DataError.Remote(type = DataError.RemoteType.REQUEST_TIMEOUT))
        429 -> AppResult.Error(DataError.Remote(type = DataError.RemoteType.TOO_MANY_REQUESTS))
        in 500..599 -> AppResult.Error(DataError.Remote(type = DataError.RemoteType.SERVER))
        else -> AppResult.Error(DataError.Remote(type = DataError.RemoteType.UNKNOWN))
    }
}