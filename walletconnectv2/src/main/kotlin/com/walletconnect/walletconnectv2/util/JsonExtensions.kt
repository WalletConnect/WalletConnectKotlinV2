@file:JvmName("JsonExtensions")

package com.walletconnect.walletconnectv2.util

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.BufferedSource

fun JsonWriter.jsonObject(objectName: String? = null, body: JsonWriter.() -> Unit) {
    if (!objectName.isNullOrBlank()) {
        name(objectName)
    }

    beginObject()
    body()
    endObject()
}

fun JsonWriter.jsonArray(objectName: String? = null, body: JsonWriter.() -> Unit) {
    if (!objectName.isNullOrBlank()) {
        name(objectName)
    }

    beginArray()
    body()
    endArray()
}

fun JsonWriter.attribute(name: String, value: Any?) {
    name(name)

    when (value) {
        is Long -> value(value)
        is Double -> value(value)
        is Number? -> value(value)
        is Boolean -> value(value)
        is Boolean? -> value(value)
        is String? -> value(value)
        is BufferedSource -> value(value)
        else -> throw Exception("Cannot Write Unsupported Type")
    }
}

fun JsonReader.jsonObject(body: JsonReader.() -> Unit) {
    beginObject()
    body()
    endObject()
}

fun JsonReader.jsonArray(body: JsonReader.() -> Unit) {
    beginArray()
    body()
    endArray()
}