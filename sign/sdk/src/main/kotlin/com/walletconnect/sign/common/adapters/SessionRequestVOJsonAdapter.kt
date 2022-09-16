@file:JvmSynthetic

package com.walletconnect.sign.common.adapters

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import org.json.JSONArray
import org.json.JSONObject
import kotlin.String

internal class SessionRequestVOJsonAdapter(moshi: Moshi) : JsonAdapter<SessionRequestVO>() {
    private val options: JsonReader.Options = JsonReader.Options.of("method", "params")
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(), "method")
    private val anyAdapter: JsonAdapter<Any> = moshi.adapter(Any::class.java, emptySet(), "params")

    override fun toString(): String = buildString(38) {
        append("GeneratedJsonAdapter(").append("SessionRequestVO").append(')')
    }

    override fun fromJson(reader: JsonReader): SessionRequestVO {
        var method: String? = null
        var params: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> method = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("method", "method", reader)
                1 -> {
                    // Moshi does not handle malformed JSON where there is a missing key for an array or object
                    val paramsAny = anyAdapter.fromJson(reader) ?: throw Util.unexpectedNull("params", "params", reader)
                    params = if (paramsAny is List<*>) {
                        upsertArray(JSONArray(), paramsAny).toString()
                    } else {
                        val paramsMap = paramsAny as Map<*, *>

                        if (paramsMap.size == 1) {
                            val paramsMapEntry: Map.Entry<*, *> = paramsAny.firstNotNullOf { it }
                            val key = paramsMapEntry.key as String

                            if (paramsMapEntry.value is List<*>) {
                                val jsonArray = upsertArray(JSONArray(), paramsMapEntry.value as List<*>).toString()

                                "\"$key\":$jsonArray"
                            } else if (paramsMapEntry.value is Map<*, *>) {
                                val jsonObject = upsertObject(JSONObject(), paramsMapEntry.value as Map<*, *>)

                                "\"$key\":$jsonObject"
                            } else {
                                upsertObject(JSONObject(), paramsMap).toString()
                            }
                        } else {
                            upsertObject(JSONObject(), paramsMap).toString()
                        }
                    }
                }
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        return SessionRequestVO(
            method = method ?: throw Util.missingProperty("method", "method", reader),
            params = params ?: throw Util.missingProperty("params", "params", reader)
        )
    }

    private fun upsertObject(rootObject: JSONObject, paramsMap: Map<*, *>): JSONObject {
        (paramsMap as Map<String, Any?>).entries.forEach { (key, value) ->
            when (value) {
                is List<*> -> rootObject.putOpt(key, upsertArray(JSONArray(), value))
                is Map<*, *> -> rootObject.putOpt(key, upsertObject(JSONObject(), value))
                is Number -> {
                    val castedNumber = if (value.toDouble() % 1 == 0.0) {
                        value.toLong()
                    } else {
                        value.toDouble()
                    }

                    rootObject.put(key, castedNumber)
                }
                else -> rootObject.putOpt(key, value ?: JSONObject.NULL)
            }
        }

        return rootObject
    }

    private fun upsertArray(rootArray: JSONArray, paramsList: List<*>): JSONArray {
        paramsList.forEach { value ->
            when (value) {
                is List<*> -> rootArray.put(upsertArray(JSONArray(), value))
                is Map<*, *> -> rootArray.put(upsertObject(JSONObject(), value))
                is String -> try {
                    when (val deserializedJson = anyAdapter.fromJson(value)) {
                        is List<*> -> rootArray.put(upsertArray(JSONArray(), deserializedJson))
                        is Map<*, *> -> rootArray.put(upsertObject(JSONObject(), deserializedJson))
                        else -> throw IllegalArgumentException("Failed Deserializing Unknown Type $value")
                    }
                } catch (e: JsonEncodingException) {
                    rootArray.put(value)
                }
                is Number -> {
                    val castedNumber = if (value.toDouble() % 1 == 0.0) {
                        value.toLong()
                    } else {
                        value.toDouble()
                    }

                    rootArray.put(castedNumber)
                }
                else -> rootArray.put(value ?: JSONObject.NULL)
            }
        }

        return rootArray
    }

    override fun toJson(writer: JsonWriter, value_: SessionRequestVO?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }

        with(writer) {
            beginObject()
            name("method")
            stringAdapter.toJson(this, value_.method)
            name("params")
            valueSink().use {
                val encodedParams: String = anyAdapter.toJson(value_.params).removeSurrounding("\"").replace("\\\"", "\"")
                it.writeUtf8(encodedParams)
            }
            endObject()
        }
    }
}