@file:JvmSynthetic

package com.walletconnect.sign.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalStateException
import java.text.NumberFormat
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
                        val stringifiedJson = stringifyJsonArray(paramsAny)
                        val jsonArray = JSONArray(stringifiedJson).toString()

                        jsonArray
                    } else {
                        val paramsMap = paramsAny as Map<*, *>

                        if (paramsMap.size == 1) {
                            val paramsMapEntry: Map.Entry<*, *> = paramsAny.firstNotNullOf { it }
                            val key = paramsMapEntry.key as String

                            if (paramsMapEntry.value is List<*>) {
                                val stringifiedJson = stringifyJsonArray(paramsMapEntry.value as List<*>)
                                val jsonArray = JSONArray(stringifiedJson).toString()

                                "\"$key\":$jsonArray"
                            } else {
                                val stringifiedJson = stringifyJsonObject(paramsMapEntry as Map<*, *>)
                                val jsonObject = JSONObject(stringifiedJson).toString()

                                "\"$key\":$jsonObject"
                            }
                        } else {
                            stringifyJsonObject(paramsMap)
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

    private fun stringifyJsonArray(paramsListEntry: List<*>): String {
        return paramsListEntry.joinToString(",", "[", "]") { item ->
            when (item) {
                is Map<*, *> -> stringifyJsonObject(item)
                is String -> "\"$item\""
                else -> throw IllegalStateException("Deserializing Unknown Type $item")
            }
        }
    }

    private fun stringifyJsonObject(paramsMap: Map<*, *>): String {
        return (paramsMap as Map<String, Any>).toList().joinToString(",", "{", "}") { (key, value) ->
            val valueString = when (value) {
                is List<*> -> stringifyJsonArray(value)
                is Map<*,*,> -> stringifyJsonObject(value)
                is String -> "\"$value\""
                is Number -> {
                    val num = NumberFormat.getInstance().apply { isGroupingUsed = false }.format(value)
                    if (num.contains(".")) {
                        num.toDouble()
                    } else {
                        num.toLong()
                    }
                }
                else -> value.toString()
            }

            "\"$key\":$valueString"
        }
    }
}