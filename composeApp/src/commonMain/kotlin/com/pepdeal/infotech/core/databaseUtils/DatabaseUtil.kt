package com.pepdeal.infotech.core.databaseUtils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

object DatabaseUtil {
    private const val PROJECT_ID = "pepdeal-1251f"
    const val DATABASE_URL =
        "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    const val STORAGE_BUCKET = "we-are-spine.firebasestorage.app"

    const val DATABASE_QUERY_URL =
        "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents:runQuery"
}

@Serializable(with = DatabaseValueSerializer::class)
sealed class DatabaseValue {
    @Serializable
    data class StringValue(val stringValue: String) : DatabaseValue()

    @Serializable
    data class ArrayValue(val values: List<StringValue> = emptyList()) : DatabaseValue()
}

@Serializable
data class DatabaseRequest(val fields: Map<String, DatabaseValue>)

@Serializable
data class DatabaseResponse(val name: String)

@Serializable
data class DatabaseDocumentsResponse(
    val documents: List<DatabaseDocument> = emptyList(),
)

@Serializable
data class DatabaseDocument(
    val name: String,
    val fields: Map<String, DatabaseValue>? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)

@Serializable
data class DatabaseQueryResponse(
    val document: DatabaseDocument? = null,
    val readTime: String? = null
)

object DatabaseValueSerializer : KSerializer<DatabaseValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DatabaseValue") {
        element<String>("stringValue", isOptional = true)
        element<JsonObject>("arrayValue", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: DatabaseValue) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("This class can be saved only by JSON")
        val jsonElement = when (value) {
            is DatabaseValue.StringValue -> JsonObject(mapOf("stringValue" to JsonPrimitive(value.stringValue)))
            is DatabaseValue.ArrayValue -> {
                val arrayObj = JsonObject(
                    mapOf("values" to JsonArray(value.values.map {
                        JsonObject(
                            mapOf(
                                "stringValue" to JsonPrimitive(
                                    it.stringValue
                                )
                            )
                        )
                    }))
                )
                JsonObject(mapOf("arrayValue" to arrayObj))
            }
        }
        jsonEncoder.encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): DatabaseValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by JSON")
        val element = jsonDecoder.decodeJsonElement()

        if (element is JsonObject) {
            return when {
                "stringValue" in element -> {
                    val value = element["stringValue"]?.jsonPrimitive?.content
                        ?: throw SerializationException("Missing stringValue content")
                    DatabaseValue.StringValue(value)
                }

                "arrayValue" in element -> {
                    val arrayObj = element["arrayValue"]?.jsonObject
                        ?: throw SerializationException("arrayValue must be a JSON object")
                    val valuesArray = arrayObj["values"]?.jsonArray ?: JsonArray(emptyList())
                    val values = valuesArray.map { jsonElem ->
                        // Expect each element to be an object with key "stringValue"
                        val obj = jsonElem.jsonObject
                        val str = obj["stringValue"]?.jsonPrimitive?.content
                            ?: throw SerializationException("Missing stringValue in array element")
                        DatabaseValue.StringValue(str)
                    }
                    DatabaseValue.ArrayValue(values)
                }

                else -> throw SerializationException("Unknown DatabaseValue type: $element")
            }
        } else {
            throw SerializationException("Expected JsonObject but found $element")
        }
    }
}

fun buildUpdateMask(vararg fields: String): String {
    return fields.joinToString("&") { "updateMask.fieldPaths=$it" }
}

fun buildCustomDatabaseQuery(
    collection: String,
    field: String,
    operator: String,
    value: String
): String {
    return buildJsonObject {
        putJsonObject("structuredQuery") {
            putJsonArray("from") {
                addJsonObject { put("collectionId", collection) }
            }
            putJsonObject("where") {
                putJsonObject("fieldFilter") {
                    putJsonObject("field") { put("fieldPath", field) }
                    put("op", operator)
                    putJsonObject("value") { put("stringValue", value) }
                }
            }
        }
    }.toString()
}

fun buildCustomDatabaseQuery(
    collection: String,
    conditions: Map<String, String>,
    limit: Int = 1
): String {
    val filters = conditions.map { (field, value) ->
        """{"fieldFilter": {"field": {"fieldPath": "$field"}, "op": "EQUAL", "value": {"stringValue": "$value"}}}"""
    }.joinToString(",")

    return """
        {
            "structuredQuery": {
                "from": [{"collectionId": "$collection"}],
                "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [$filters]
                    }
                },
                "limit": $limit
            }
        }
    """.trimIndent()
}


