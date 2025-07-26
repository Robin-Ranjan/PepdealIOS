package com.pepdeal.infotech.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pepdeal.infotech.core.domain.AppResult
import com.pepdeal.infotech.core.domain.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class PreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) :
    PreferencesRepository {

    override suspend fun saveString(
        key: Preferences.Key<String>,
        value: String
    ): Flow<AppResult<Unit, DataError.Local>> = flow {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
            emit(AppResult.Error(DataError.Local(type = DataError.LocalType.UNKNOWN)))
        }
    }

    override suspend fun getString(key: Preferences.Key<String>): String? {
        return dataStore.data.first()[key]
    }

    override suspend fun deleteKey(key: Preferences.Key<String>): Flow<AppResult<Unit, DataError.Local>> =
        flow {
            try {
                dataStore.edit { preferences ->
                    preferences.remove(key)
                }
                emit(AppResult.Success(Unit))
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
                emit(AppResult.Error(DataError.Local(type = DataError.LocalType.UNKNOWN)))
            }
        }

    override suspend fun <T : Any> saveDataClass(
        key: Preferences.Key<String>,
        data: T,
        serializer: KSerializer<T>,
        json: Json
    ): Flow<AppResult<Unit, DataError.Local>> = flow {
        try {
            val jsonString = json.encodeToString(serializer, data)
            saveString(key, jsonString)
            dataStore.edit { it[key] = jsonString }
            emit(AppResult.Success(Unit))

        } catch (e: Exception) {
            e.printStackTrace()
            emit(AppResult.Error(DataError.Local(DataError.LocalType.UNKNOWN)))
        }
    }

    override suspend fun <T : Any> getDataClass(
        key: Preferences.Key<String>,
        serializer: KSerializer<T>,
        json: Json
    ): T? {
        return try {
            val jsonString = dataStore.data.first()[key] ?: return null
            json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun <T : Any> updateDataClass(
        key: Preferences.Key<String>,
        serializer: KSerializer<T>,
        json: Json,
        update: (T) -> T
    ) {
        dataStore.edit { prefs ->
            val existingJson = prefs[key]
            if (existingJson != null) {
                val existingObj = json.decodeFromString(serializer, existingJson)
                val updatedObj = update(existingObj)
                prefs[key] = json.encodeToString(serializer, updatedObj)
            }
        }
    }

    override suspend fun clearAll(): Flow<AppResult<Unit, DataError.Local>> = flow {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            emit(AppResult.Success(Unit))
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()
            emit(AppResult.Error(DataError.Local(DataError.LocalType.UNKNOWN)))
        }
    }

}