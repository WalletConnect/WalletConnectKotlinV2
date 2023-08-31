@file:JvmSynthetic

package com.walletconnect.android.internal.common.adapter

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Constructor
import kotlin.Int
import kotlin.Long
import kotlin.String

//TODO: revisit and make more scalable
internal class JsonRpcResultAdapter(val moshi: Moshi) : JsonAdapter<JsonRpcResponse.JsonRpcResult>() {
    private val options: JsonReader.Options = JsonReader.Options.of("id", "jsonrpc", "result")
    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, emptySet(), "id")
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(), "jsonrpc")
    private val anyAdapter: JsonAdapter<Any> = moshi.adapter(Any::class.java, emptySet(), "result")
    private val approvalParamsAdapter: JsonAdapter<CoreSignParams.ApprovalParams> =
        moshi.adapter(CoreSignParams.ApprovalParams::class.java, emptySet(), "result")
    private val cacaoAdapter: JsonAdapter<CoreAuthParams.ResponseParams> =
        moshi.adapter(CoreAuthParams.ResponseParams::class.java, emptySet(), "result")
    private val notifySubscribeUpdateParamsAdapter: JsonAdapter<CoreNotifyParams.UpdateParams> =
        moshi.adapter(CoreNotifyParams.UpdateParams::class.java, emptySet(), "result")
    private val chatNotifyResponseAuthParamsAdapter: JsonAdapter<ChatNotifyResponseAuthParams.ResponseAuth> =
        moshi.adapter(ChatNotifyResponseAuthParams.ResponseAuth::class.java, emptySet(), "result")

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
                    result = when {
                        runCatching { approvalParamsAdapter.fromJson(reader.peekJson()) }.isSuccess -> {
                            approvalParamsAdapter.fromJson(reader)
                        }

                        runCatching { cacaoAdapter.fromJson(reader.peekJson()) }.isSuccess -> {
                            cacaoAdapter.fromJson(reader)
                        }

                        runCatching { notifySubscribeUpdateParamsAdapter.fromJson(reader.peekJson()) }.isSuccess -> {
                            notifySubscribeUpdateParamsAdapter.fromJson(reader)
                        }

                        else -> anyAdapter.fromJson(reader)
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
                this@JsonRpcResultAdapter.constructorRef
                    ?: JsonRpcResponse.JsonRpcResult::class.java.getDeclaredConstructor(
                        Long::class.javaPrimitiveType,
                        String::class.java, Any::class.java, Int::class.javaPrimitiveType,
                        Util.DEFAULT_CONSTRUCTOR_MARKER
                    ).also { this@JsonRpcResultAdapter.constructorRef = it }
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
                val approvalParamsString =
                    approvalParamsAdapter.toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(approvalParamsString)
                }
            }

            (value_.result as? CoreAuthParams.ResponseParams) != null -> {
                val responseParamsString =
                    cacaoAdapter.toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(responseParamsString)
                }
            }

            (value_.result as? ChatNotifyResponseAuthParams.ResponseAuth) != null -> {
                val notifySubscribeResponseParamsString = chatNotifyResponseAuthParamsAdapter.toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(notifySubscribeResponseParamsString)
                }
            }

            (value_.result as? CoreNotifyParams.UpdateParams) != null -> {
                val notifySubscribeUpdateParamsString = notifySubscribeUpdateParamsAdapter.toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(notifySubscribeUpdateParamsString)
                }
            }

            value_.result is String && value_.result.startsWith("{") -> {
                writer.valueSink().use {
                    it.writeUtf8(JSONObject(value_.result).toString())
                }
            }

            value_.result is String && value_.result.startsWith("[") -> {
                writer.valueSink().use {
                    it.writeUtf8(JSONArray(value_.result).toString())
                }
            }
            else -> anyAdapter.toJson(writer, value_.result)
        }

        writer.endObject()
    }
}
