package com.pepdeal.infotech.core.domain

sealed interface DataError : Error {
    data class Remote(val type: RemoteType, val message: String? = null) : DataError
    data class Local(val type: LocalType, val message: String? = null) : DataError

    enum class RemoteType {
        REQUEST_TIMEOUT, TOO_MANY_REQUESTS, NO_INTERNET, SERVER, SERIALIZATION, UNKNOWN, NOT_FOUND, PASS_INCORRECT, EMPTY_RESULT
    }

    enum class LocalType {
        DISK_FULL, UNKNOWN
    }
}