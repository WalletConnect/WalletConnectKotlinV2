@file:Suppress(
    "DEPRECATION", "unused", "ClassName", "REDUNDANT_PROJECTION",
    "RedundantExplicitType", "LocalVariableName", "RedundantVisibilityModifier",
    "PLATFORM_CLASS_MAPPED_TO_KOTLIN", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION"
)

package com.walletconnect.web3.inbox.json_rpc


import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.internal.Util
import java.lang.reflect.Constructor
import kotlin.Int
import kotlin.Long
import kotlin.String

internal class Web3InboxRPCCallPushSubscriptionJsonAdapter(
    moshi: Moshi,
) : JsonAdapter<Web3InboxRPC.Call.Push.Subscription>() {
    private val options: JsonReader.Options = JsonReader.Options.of(
        "id", "jsonrpc", "method",
        "params"
    )

    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, emptySet(), "id")

    private val stringAdapter: JsonAdapter<String> = moshi.adapter(
        String::class.java, emptySet(),
        "jsonrpc"
    )

    private val resultAdapter: JsonAdapter<Web3InboxParams.Call.Push.Subscription.ResultParams> =
        moshi.adapter(Web3InboxParams.Call.Push.Subscription.ResultParams::class.java, emptySet(), "params")

    private val errorAdapter: JsonAdapter<Web3InboxParams.Call.Push.Subscription.ErrorParams> =
        moshi.adapter(Web3InboxParams.Call.Push.Subscription.ErrorParams::class.java, emptySet(), "params")

    @Volatile
    private var constructorRef: Constructor<Web3InboxRPC.Call.Push.Subscription>? = null

    public override fun toString(): String = buildString(57) {
        append("GeneratedJsonAdapter(").append("Web3InboxRPC.Call.Push.Subscription").append(')')
    }

    public override fun fromJson(reader: JsonReader): Web3InboxRPC.Call.Push.Subscription {
        var id: Long? = 0L
        var jsonrpc: String? = null
        var method: String? = null
        var params: Web3InboxParams.Call.Push.Subscription? = null
        var mask0 = -1
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> {
                    id = longAdapter.fromJson(reader) ?: throw Util.unexpectedNull("id", "id", reader)
                    // $mask = $mask and (1 shl 0).inv()
                    mask0 = mask0 and 0xfffffffe.toInt()
                }
                1 -> {
                    jsonrpc = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull(
                        "jsonrpc",
                        "jsonrpc", reader
                    )
                    // $mask = $mask and (1 shl 1).inv()
                    mask0 = mask0 and 0xfffffffd.toInt()
                }
                2 -> {
                    method = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull(
                        "method", "method",
                        reader
                    )
                    // $mask = $mask and (1 shl 2).inv()
                    mask0 = mask0 and 0xfffffffb.toInt()
                }
                3 -> params = when {
                    runCatching { resultAdapter.fromJson(reader.peekJson()) }.isSuccess -> {
                        resultAdapter.fromJson(reader)
                    }
                    else -> {
                        errorAdapter.fromJson(reader)
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
        if (mask0 == 0xfffffff8.toInt()) {
            // All parameters with defaults are set, invoke the constructor directly
            return Web3InboxRPC.Call.Push.Subscription(
                id = id as Long,
                jsonrpc = jsonrpc as String,
                method = method as String,
                params = params ?: throw Util.missingProperty("params", "params", reader)
            )
        } else {
            // Reflectively invoke the synthetic defaults constructor
            @Suppress("UNCHECKED_CAST")
            val localConstructor: Constructor<Web3InboxRPC.Call.Push.Subscription> =
                this.constructorRef ?: Web3InboxRPC.Call.Push.Subscription::class.java.getDeclaredConstructor(
                    Long::class.javaPrimitiveType,
                    String::class.java, String::class.java,
                    Web3InboxParams.Call.Push.Subscription::class.java, Int::class.javaPrimitiveType,
                    Util.DEFAULT_CONSTRUCTOR_MARKER
                ).also { this.constructorRef = it }
            return localConstructor.newInstance(
                id,
                jsonrpc,
                method,
                params ?: throw Util.missingProperty("params", "params", reader),
                mask0,
                /* DefaultConstructorMarker */ null
            )
        }
    }

    public override fun toJson(writer: JsonWriter, value_: Web3InboxRPC.Call.Push.Subscription?):
            Unit {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("id")
        longAdapter.toJson(writer, value_.id)
        writer.name("jsonrpc")
        stringAdapter.toJson(writer, value_.jsonrpc)
        writer.name("method")
        stringAdapter.toJson(writer, value_.method)
        when (value_.params) {
            is Web3InboxParams.Call.Push.Subscription.ErrorParams -> {
                writer.name("params")
                errorAdapter.toJson(writer, value_.params)
            }
            is Web3InboxParams.Call.Push.Subscription.ResultParams -> {
                writer.name("params")
                resultAdapter.toJson(writer, value_.params)
            }
        }
        writer.endObject()
    }
}
