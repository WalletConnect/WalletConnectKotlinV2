package com.walletconnect.walletconnectv2.core.adapters

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import okio.use
import java.lang.reflect.Constructor

internal class SessionSettlementVO_SessionRequestJsonAdapter(
    moshi: Moshi,
) : JsonAdapter<SessionSettlementVO.SessionRequest>() {
    private val options: JsonReader.Options = JsonReader.Options.of("id", "jsonrpc", "method",
        "params")

    private val longAdapter: JsonAdapter<Long> = moshi.adapter(Long::class.java, emptySet(), "id")

    private val stringAdapter: JsonAdapter<String> = moshi.adapter(String::class.java, emptySet(),
        "jsonrpc")

    private val sessionPayloadParamsAdapter: JsonAdapter<SessionParamsVO.SessionRequestParams> =
        moshi.adapter(SessionParamsVO.SessionRequestParams::class.java, emptySet(), "params")

    @Volatile
    private var constructorRef: Constructor<SessionSettlementVO.SessionRequest>? = null

    override fun toString(): String = buildString(60) {
        append("GeneratedJsonAdapter(").append("PostSettlementSessionVO.SessionPayload").append(')')
    }

    @JvmSynthetic
    @FromJson
    @SessionSettlementVO_SessionRequestJsonAdapter.Qualifier
    override fun fromJson(reader: JsonReader): SessionSettlementVO.SessionRequest {
        var id: Long? = null
        var jsonrpc: String? = null
        var method: String? = null
        var params: SessionParamsVO.SessionRequestParams? = null
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
                    method = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull("method", "method",
                        reader)
                    // $mask = $mask and (1 shl 2).inv()
                    mask0 = mask0 and 0xfffffffb.toInt()
                }
                3 -> params = sessionPayloadParamsAdapter.fromJson(reader) ?: throw Util.unexpectedNull("params", "params", reader)
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        if (mask0 == 0xfffffff9.toInt()) {
            // All parameters with defaults are set, invoke the constructor directly
            return SessionSettlementVO.SessionRequest(
                id = id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc = jsonrpc as String,
                method = method as String,
                params = params ?: throw Util.missingProperty("params", "params", reader)
            )
        } else {
            // Reflectively invoke the synthetic defaults constructor
            @Suppress("UNCHECKED_CAST")
            val localConstructor: Constructor<SessionSettlementVO.SessionRequest> =
                this.constructorRef ?: SessionSettlementVO.SessionRequest::class.java.getDeclaredConstructor(Long::class.javaPrimitiveType,
                    String::class.java, String::class.java, SessionParamsVO.SessionRequestParams::class.java,
                    Int::class.javaPrimitiveType, Util.DEFAULT_CONSTRUCTOR_MARKER).also {
                    this.constructorRef = it
                }
            return localConstructor.newInstance(
                id ?: throw Util.missingProperty("id", "id", reader),
                jsonrpc,
                method,
                params ?: throw Util.missingProperty("params", "params", reader),
                mask0,
                /* DefaultConstructorMarker */ null
            )
        }
    }

    @JvmSynthetic
    @ToJson
    override fun toJson(writer: JsonWriter, value_: SessionSettlementVO.SessionRequest?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.isLenient = true
        writer.beginObject()
        writer.name("id")
        longAdapter.toJson(writer, value_.id)
        writer.name("jsonrpc")
        stringAdapter.toJson(writer, value_.jsonrpc)
        writer.name("method")
        stringAdapter.toJson(writer, value_.method)
        writer.name("params")

        val payloadParams = sessionPayloadParamsAdapter.toJson(value_.params).run {
            replace("\\\"", "\"")
        }.let { sessionPayload ->
            if (sessionPayload.contains("\"params\":") && sessionPayload.contains(",\"chainId\"")) {
                val startIndex = sessionPayload.indexOf("\"params\":") + "\"params\":".length
                val endIndex = sessionPayload.indexOf("},\"chainId\"")

                sessionPayload.replace(sessionPayload.substring(startIndex, endIndex), sessionPayload.substring(startIndex, endIndex).removeSurrounding("\""))
            } else {
                sessionPayload
            }
        }

        writer.valueSink().use {
            it.writeUtf8(payloadParams)
        }

        writer.endObject()
    }

    @Retention(AnnotationRetention.RUNTIME)
    @JsonQualifier
    internal annotation class Qualifier
}