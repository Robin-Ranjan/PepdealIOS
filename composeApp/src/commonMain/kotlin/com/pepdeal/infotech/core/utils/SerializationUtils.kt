package com.pepdeal.infotech.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer


val AppJson = Json {
    prettyPrint = false
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

inline fun <reified T> serialize(data: T, json: Json = AppJson): String {
    return json.encodeToString(data)
}

inline fun <reified T> deserialize(jsonString: String, json: Json = AppJson): T {
    return json.decodeFromString(jsonString)
}

suspend inline fun <reified T> serializeList(
    data: List<T>,
    json: Json = AppJson
): String {
    return withContext(Dispatchers.Default) {
        json.encodeToString(ListSerializer(serializer<T>()), data)
    }
}


suspend inline fun <reified T> deserializeList(
    jsonString: String,
    json: Json = AppJson
): List<T> {
    return withContext(Dispatchers.Default) {
        json.decodeFromString(ListSerializer(serializer<T>()), jsonString)
    }
}