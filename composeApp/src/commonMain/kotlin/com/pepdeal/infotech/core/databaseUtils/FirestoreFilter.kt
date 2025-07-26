package com.pepdeal.infotech.core.databaseUtils

data class FirestoreFilter(
    val field: String,
    val value: Any,
    val op: FirestoreOperator = FirestoreOperator.EQUAL
)

enum class FirestoreOperator {
    EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,IN, NOT_IN, ARRAY_CONTAINS_ANY,
    LESS_THAN, LESS_THAN_OR_EQUAL, ARRAY_CONTAINS
}
