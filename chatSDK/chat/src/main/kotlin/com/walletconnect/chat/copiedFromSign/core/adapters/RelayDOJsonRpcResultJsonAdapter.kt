@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.core.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import com.walletconnect.sign.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.sign.json_rpc.model.RelayerDO
import org.json.JSONArray
import org.json.JSONObject
import com.walletconnect.chat.copiedFromSign.json_rpc.model.RelayerDO
import com.walletconnect.chat.core.model.vo.clientsync.params.ChatParamsVO
import java.lang.reflect.Constructor
import kotlin.Int
import kotlin.Long
import kotlin.String

//todo: when extracted to core code for serialising both SessionParamsVO.ApprovalParams and ChatParamsVO.ApprovalParams
internal class RelayDOJsonRpcResultJsonAdapter(moshi: Moshi) : JsonAdapter<RelayerDO.JsonRpcResponse.JsonRpcResult>() {
    private val options: JsonReader.Options = JsonReader.Options.of("id", "jsonrpc", "result")
    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, emptySet(), "id")
    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(), "jsonrpc")
    private val booleanAdapter: JsonAdapter<Long> = moshi.adapter(Boolean::class.java, emptySet(), "result")
    private val anyAdapter: JsonAdapter<Any> = moshi.adapter(Any::class.java, emptySet(), "result")
    private val acceptanceParamsAdapter: JsonAdapter<ChatParamsVO.AcceptanceParams> =
        moshi.adapter(ChatParamsVO.AcceptanceParams::class.java)

    @Volatile
    private var constructorRef: Constructor<RelayerDO.JsonRpcResponse.JsonRpcResult>? = null

    override fun toString(): String = buildString(59) {
        append("GeneratedJsonAdapter(").append("RelayDO.JsonRpcResponse.JsonRpcResult").append(')')
    }

    override fun fromJson(reader: JsonReader): RelayerDO.JsonRpcResponse.JsonRpcResult {
        var id: Long? = null
        var jsonrpc: String? = null
        var result: Any? = null
        var mask0 = -1
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> id = longAdapter.fromJson(reader) ?: throw Util.unexpectedNull("id", "id", reader)
                1 -> {
                    jsonrpc = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("jsonrpc",
                        "jsonrpc", reader)
                    // $mask = $mask and (1 shl 1).inv()
                    mask0 = mask0 and 0xfffffffd.toInt()
                }
                2 -> {
                    result = try {
                        acceptanceParamsAdapter.fromJson(reader)
                    } catch (e: Exception) {
                        anyAdapter.fromJson(reader)
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
            return RelayerDO.JsonRpcResponse.JsonRpcResult(
                id = id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc = jsonrpc as String,
                result = result ?: throw Util.missingProperty("result", "result", reader)
            )
        } else {
            // Reflectively invoke the synthetic defaults constructor
            @Suppress("UNCHECKED_CAST")
            val localConstructor: Constructor<RelayerDO.JsonRpcResponse.JsonRpcResult> =
                this.constructorRef
                    ?: RelayerDO.JsonRpcResponse.JsonRpcResult::class.java.getDeclaredConstructor(Long::class.javaPrimitiveType,
                        String::class.java, Any::class.java, Int::class.javaPrimitiveType,
                        Util.DEFAULT_CONSTRUCTOR_MARKER).also { this.constructorRef = it }
            return localConstructor.newInstance(
                id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc,
                result ?: throw Util.missingProperty("result", "result", reader),
                mask0,
                /* DefaultConstructorMarker */ null
            )
        }
    }

    override fun toJson(writer: JsonWriter, value_: RelayerDO.JsonRpcResponse.JsonRpcResult?) {
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
            (value_.result as? ChatParamsVO.AcceptanceParams) != null -> {
                val approvalParamsString = acceptanceParamsAdapter.toJson(value_.result)
                writer.valueSink().use {
                    it.writeUtf8(approvalParamsString)
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
