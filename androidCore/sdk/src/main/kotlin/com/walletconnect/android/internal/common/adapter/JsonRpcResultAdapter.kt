@file:JvmSynthetic

package com.walletconnect.android.internal.common.adapter

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Constructor
import kotlin.Int
import kotlin.Long
import kotlin.String

// TODO: figure out how to share this with other modules
internal class JsonRpcResultAdapter(val moshi: Moshi) : JsonAdapter<JsonRpcResponse.JsonRpcResult>() {

    private val options: JsonReader.Options = JsonReader.Options.of("id", "jsonrpc", "result")
    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, emptySet(), "id")
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(), "jsonrpc")
    private val anyAdapter: JsonAdapter<Any> = moshi.adapter(Any::class.java, emptySet(), "result")

//    private val approvalParamsAdapter: JsonAdapter<CoreSignParams.ApprovalParams> = moshi.adapter(CoreSignParams.ApprovalParams::class.java)
//    private val cacaoAdapter: JsonAdapter<CoreAuthParams.ResponseParams> = moshi.adapter(CoreAuthParams.ResponseParams::class.java)
//    private val acceptanceParamsAdapter: JsonAdapter<CoreChatParams.AcceptanceParams> = moshi.adapter(CoreChatParams.AcceptanceParams::class.java)


    init {

        println("kobe; Adding adapters 1")

        moshi.newBuilder()
            .addLast(CoreSignParams.ApprovalParams::class.java)
            .addLast(CoreAuthParams.ResponseParams::class.java)
            .addLast(CoreChatParams.AcceptanceParams::class.java)
            .build()

        println("kobe; Adding adapters 2")
    }

    @Volatile
    private var constructorRef: Constructor<JsonRpcResponse.JsonRpcResult>? = null

    override fun toString(): String = buildString(59) {
        append("GeneratedJsonAdapter(").append("RelayDO.JsonRpcResponse.JsonRpcResult").append(')')
    }

    override fun fromJson(reader: JsonReader): JsonRpcResponse.JsonRpcResult {
        var id: Long? = null
        var jsonrpc: String? = null
        var result: Any? = null
        var mask0 = -1
        reader.beginObject()

        while (reader.hasNext()) {

            when (reader.selectName(options)) {

                0 -> id = longAdapter.fromJson(reader) ?: throw Util.unexpectedNull("id", "id", reader)
                1 -> {
                    jsonrpc = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("jsonrpc", "jsonrpc", reader)
                    // $mask = $mask and (1 shl 1).inv()
                    mask0 = mask0 and 0xfffffffd.toInt()
                }
                2 -> {

                    result = moshi.adapter(CoreAuthParams.ResponseParams::class.java).fromJson(reader)

//                    try {
//
//                        approvalParamsAdapter.fromJson(reader)
//
//                    } catch (e: JsonDataException) {
//
//                        cacaoAdapter.fromJson(reader)
//
////                        try {
////                            cacaoAdapter.fromJson(reader)
////                        } catch (e: JsonDataException) {
////
////                            try {
////
////                                acceptanceParamsAdapter.fromJson(reader)
////
////                            } catch (e: JsonDataException) {
////                                anyAdapter.fromJson(reader)
////                            }
////                        }
//                    }
                }
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        if (mask0 == 0xfffffffd.toInt()) {
            // All parameters with defaults are set, invoke the constructor directly
            return JsonRpcResponse.JsonRpcResult(
                id = id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc = jsonrpc as String,
                result = result ?: throw Util.missingProperty("result", "result", reader)
            )
        } else {
            // Reflectively invoke the synthetic defaults constructor
            @Suppress("UNCHECKED_CAST")
            val localConstructor: Constructor<JsonRpcResponse.JsonRpcResult> =
                this.constructorRef
                    ?: JsonRpcResponse.JsonRpcResult::class.java.getDeclaredConstructor(
                        Long::class.javaPrimitiveType,
                        String::class.java, Any::class.java, Int::class.javaPrimitiveType,
                        Util.DEFAULT_CONSTRUCTOR_MARKER
                    ).also { this.constructorRef = it }
            return localConstructor.newInstance(
                id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc,
                result ?: throw Util.missingProperty("result", "result", reader),
                mask0,
                /* DefaultConstructorMarker */ null
            )
        }
    }

    override fun toJson(writer: JsonWriter, value_: JsonRpcResponse.JsonRpcResult?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }

        writer.beginObject()
        writer.name("id")
        longAdapter.toJson(writer, value_.id)
        writer.name("jsonrpc")
        stringAdapter.toJson(writer, value_.jsonrpc)
        writer.name("result")

        when {
            (value_.result as? CoreSignParams.ApprovalParams) != null -> {
                val approvalParamsString = moshi.adapter(CoreSignParams.ApprovalParams::class.java).toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(approvalParamsString)
                }
            }

            (value_.result as? CoreAuthParams.ResponseParams) != null -> {
                val responseParamsString = moshi.adapter(CoreAuthParams.ResponseParams::class.java).toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(responseParamsString)
                }
            }

            (value_.result as? CoreChatParams.AcceptanceParams) != null -> {
                val approvalParamsString = moshi.adapter(CoreChatParams.AcceptanceParams::class.java).toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(approvalParamsString)
                }
            }

            value_.result is String && (value_.result as String).startsWith("{") -> {
                writer.valueSink().use {
                    it.writeUtf8(JSONObject(value_.result as String).toString())
                }
            }
            value_.result is String && (value_.result as String).startsWith("[") -> {
                writer.valueSink().use {
                    it.writeUtf8(JSONArray(value_.result as String).toString())
                }
            }
            else -> anyAdapter.toJson(writer, value_.result)
        }
        writer.endObject()
    }
}
