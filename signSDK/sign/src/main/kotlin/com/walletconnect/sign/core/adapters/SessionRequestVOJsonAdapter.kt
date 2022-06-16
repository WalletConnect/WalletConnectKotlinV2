package com.walletconnect.sign.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
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
        var params: Any? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> method = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("method",
                    "method", reader)
                1 -> params = anyAdapter.fromJson(reader) ?: throw Util.unexpectedNull("params", "params",
                    reader)
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
}

val test = """
    //wc(
        "params" : {
           "method":"payload",
           "params": "[]"
        }
)
"""