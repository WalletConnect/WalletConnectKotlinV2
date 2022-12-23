package com.walletconnect.android.internal

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.adapter.JsonRpcResultAdapter
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.params.Cacao
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName

internal class JsonRpcResponseJsonRpcResultJsonAdapterTest {

    @Test
    fun `test sign params to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val approvalParams =
            CoreSignParams.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124")
        val jsonResult = JsonRpcResponse.JsonRpcResult(
            id = 1L,
            jsonrpc = "2.0",
            result = approvalParams
        )

        val result = adapter.toJson(jsonResult)
        println()
        println(result)
        println()
    }

    @Test
    fun `test sign params from json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val approvalParamsJsonResult = JsonRpcResponse.JsonRpcResult(
            id = 11L,
            result = CoreSignParams.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124")
        )
        val resultString = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)
        val result = adapter.fromJson(resultString)
        result is JsonRpcResponse.JsonRpcResult
        println()
        println(result)
        println()
    }

    @Test
    fun `test auth params to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val authParams = CoreAuthParams.ResponseParams(
            header = Cacao.Header("t"),
            payload = Cacao.Payload("iss", "domain", "aud", "version", "nonce", "iat", "nbf", "exp", "statement", "id", listOf("res")),
            signature = Cacao.Signature("t", "s")
        )
        val jsonResult = JsonRpcResponse.JsonRpcResult(
            id = 1L,
            jsonrpc = "2.0",
            result = authParams
        )
        val result = adapter.toJson(jsonResult)
        println()
        println(result)
        println()
    }

    @Test
    fun `test auth params from json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }

        }.build()

        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val authParams = CoreAuthParams.ResponseParams(
            header = Cacao.Header("t"),
            payload = Cacao.Payload("iss", "domain", "aud", "version", "nonce", "iat", "nbf", "exp", "statement", "id", listOf("res")),
            signature = Cacao.Signature("t", "s")
        )

        val authParamsJsonResult = JsonRpcResponse.JsonRpcResult(id = 11L, result = authParams)
        val resultString = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).toJson(authParamsJsonResult)
        val result = adapter.fromJson(resultString)
        result is JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }

    @Test
    fun `test chat params to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val chatParams = CoreChatParams.AcceptanceParams(publicKey = "pubKey")
        val jsonResult = JsonRpcResponse.JsonRpcResult(
            id = 1L,
            jsonrpc = "2.0",
            result = chatParams
        )
        val result = adapter.toJson(jsonResult)
        println()
        println(result)
        println()
    }

    @Test
    fun `test chat params from json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val chatParams = CoreChatParams.AcceptanceParams(publicKey = "pubKey")
        val authParamsJsonResult = JsonRpcResponse.JsonRpcResult(id = 11L, result = chatParams)
        val resultString = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).toJson(authParamsJsonResult)
        val result = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).fromJson(resultString)
        result is JsonRpcResponse.JsonRpcResult
        println()
        println(result)
        println()
    }

    @Test
    fun `test from json with boolean`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)

        val approvalParamsJsonResult = JsonRpcResponse.JsonRpcResult(id = 11L, result = true)
        val resultString = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)

        val result = adapter.fromJson(resultString)
        result is JsonRpcResponse.JsonRpcResult
        println()
        println(result)
        println()
    }
}