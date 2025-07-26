package com.pepdeal.infotech.dataStore

import androidx.datastore.preferences.core.Preferences
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import com.pepdeal.infotech.core.utils.AppJson
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

interface PreferencesRepository {
    suspend fun saveString(
        key: Preferences.Key<String>,
        value: String
    ): Flow<AppResult<Unit, DataError.Local>>

    suspend fun getString(key: Preferences.Key<String>): String?
    suspend fun deleteKey(key: Preferences.Key<String>): Flow<AppResult<Unit, DataError.Local>>

    suspend fun <T : Any> saveDataClass(
        key: Preferences.Key<String>,
        data: T,
        serializer: KSerializer<T>,
        json: Json = AppJson
    ): Flow<AppResult<Unit, DataError.Local>>

    suspend fun <T : Any> getDataClass(
        key: Preferences.Key<String>,
        serializer: KSerializer<T>,
        json: Json = AppJson
    ): T?

    suspend fun <T : Any> updateDataClass(
        key: Preferences.Key<String>,
        serializer: KSerializer<T>,
        json: Json = AppJson,
        update: (T) -> T
    )

    suspend fun clearAll(): Flow<AppResult<Unit, DataError.Local>>
}