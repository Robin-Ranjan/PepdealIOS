package com.pepdeal.infotech.core.databaseUtils

import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import lottie.lottieComposition.toNSData
import platform.Foundation.NSData

fun buildFirestoreQuery(
    collection: String,
    filters: List<FirestoreFilter>,
    limit: Int? = null
): JsonObject {
    return buildJsonObject {
        putJsonObject("structuredQuery") {
            putJsonArray("from") {
                addJsonObject { put("collectionId", collection) }
            }

            if (filters.isNotEmpty()) {
                val isSingle = filters.size == 1
                if (isSingle) {
                    val filter = filters[0]
                    putJsonObject("where") {
                        putJsonObject("fieldFilter") {
                            putJsonObject("field") { put("fieldPath", filter.field) }
                            put("op", filter.op.name)
                            putJsonObject("value") {
                                writeFirestoreValue(this, filter.value, filter.op)
                            }
                        }
                    }
                } else {
                    putJsonObject("where") {
                        putJsonObject("compositeFilter") {
                            put("op", "AND")
                            putJsonArray("filters") {
                                filters.forEach { filter ->
                                    addJsonObject {
                                        putJsonObject("fieldFilter") {
                                            putJsonObject("field") { put("fieldPath", filter.field) }
                                            put("op", filter.op.name)
                                            putJsonObject("value") {
                                                writeFirestoreValue(this, filter.value, filter.op)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            limit?.let { put("limit", it) }
        }
    }
}

private fun writeFirestoreValue(builder: JsonObjectBuilder, value: Any, op: FirestoreOperator) {
    when {
        value is String -> builder.put("stringValue", value)
        value is List<*> && op == FirestoreOperator.IN -> {
            builder.putJsonObject("arrayValue") {
                putJsonArray("values") {
                    value.filterIsInstance<String>().forEach { item ->
                        addJsonObject { put("stringValue", item) }
                    }
                }
            }
        }
        else -> error("Unsupported value type or operator: $value, $op")
    }
}


fun buildFirestorePatchUrl(
    baseUrl: String = DatabaseUtil.DATABASE_URL,
    collection: String,
    documentId: String,
    fields: List<String>
): String {
    val maskParams = fields.joinToString("&") { "updateMask.fieldPaths=$it" }
    return "$baseUrl/$collection/$documentId?$maskParams"
}


fun buildFirestorePatchBody(
    fields: Map<String, Any>,
    updatedAt: String = getCurrentTimeStamp()
): String {
    val json = buildJsonObject {
        putJsonObject("fields") {
            fields.forEach { (key, value) ->
                putJsonObject(key) {
                    when (value) {
                        is String -> put("stringValue", value)
                        is Boolean -> put("booleanValue", value)
                        is Number -> put("integerValue", value.toString())
                        is Set<*> -> {
                            val valuesArray = buildJsonArray {
                                value.forEach { element ->
                                    if (element is String) {
                                        addJsonObject {
                                            put("stringValue", element)
                                        }
                                    }
                                }
                            }
                            putJsonObject("arrayValue") {
                                put("values", valuesArray)
                            }
                        }
                        is List<*> -> {
                            val valuesArray = buildJsonArray {
                                value.forEach { element ->
                                    when (element) {
                                        is String -> addJsonObject { put("stringValue", element) }
                                        is Number -> addJsonObject { put("integerValue", element.toString()) }
                                        is Boolean -> addJsonObject { put("booleanValue", element) }
                                        else -> addJsonObject { put("stringValue", element.toString()) }
                                    }
                                }
                            }
                            putJsonObject("arrayValue") {
                                put("values", valuesArray)
                            }
                        }
                        else -> put("stringValue", value.toString())
                    }
                }
            }
            // Always include updatedAt
            putJsonObject("updatedAt") {
                put("stringValue", updatedAt)
            }
        }
    }
    return json.toString()
}


fun stringValueOrNull(value: String?): JsonObject = buildJsonObject {
    put("stringValue", value ?: "")
}

fun getCurrentTimeStamp(): String {
    return Clock.System.now().toEpochMilliseconds().toString()
}

fun String.toRequestBody(contentType: String = "application/json"): NSData =
    this.encodeToByteArray().toNSData()

