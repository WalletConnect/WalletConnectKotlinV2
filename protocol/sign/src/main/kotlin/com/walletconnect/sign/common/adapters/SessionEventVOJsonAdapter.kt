package com.walletconnect.sign.common.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import org.json.JSONArray
import org.json.JSONObject

internal class SessionEventVOJsonAdapter(moshi: Moshi) : JsonAdapter<SessionEventVO>() {
    private val options: JsonReader.Options = JsonReader.Options.of("name", "data")
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(), "name")
    private val anyAdapter: JsonAdapter<Any> = moshi.adapter(Any::class.java, emptySet(), "data")

    override fun toString(): String = buildString(38) {
        append("GeneratedJsonAdapter(").append("SessionEventVO").append(')')
    }

    override fun fromJson(reader: JsonReader): SessionEventVO {
        var name: String? = null
        var data: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> name = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("name", "name", reader)
                1 -> {
                    // Moshi does not handle malformed JSON where there is a missing key for an array or object
                    val dataAny = anyAdapter.fromJson(reader) ?: throw Util.unexpectedNull("data", "data", reader)

                    data = if (dataAny is List<*>) {
                        upsertArray(JSONArray(), dataAny).toString()
                    } else if (dataAny is Map<*, *>) {
                        val paramsMap = dataAny as Map<*, *>
                        upsertObject(JSONObject(), paramsMap).toString()
                    } else {
                        if (dataAny is Number) {
                            val castedNumber = if (dataAny.toDouble() % 1 == 0.0) {
                                dataAny.toLong()
                            } else {
                                dataAny.toDouble()
                            }
                            castedNumber.toString()
                        } else {
                            dataAny.toString()
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
        return SessionEventVO(
            name = name ?: throw Util.missingProperty("name", "name", reader),
            data = data ?: throw Util.missingProperty("data", "data", reader),
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
                        is Number -> rootArray.put(value.toString())
                        else -> throw IllegalArgumentException("Failed Deserializing Unknown Type $value")
                    }
                } catch (e: Exception) {
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

    override fun toJson(writer: JsonWriter, value_: SessionEventVO?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }

        with(writer) {
            beginObject()
            name("name")
            stringAdapter.toJson(this, value_.name)
            name("data")
            valueSink().use {
                val encodedParams: String = anyAdapter.toJson(value_.data)
                    .removeSurrounding("\"")
                    .replace("\\\"", "\"")
                    .replace("\\\\\"", "\\\"")

                it.writeUtf8(encodedParams)
            }
            endObject()
        }
    }
}