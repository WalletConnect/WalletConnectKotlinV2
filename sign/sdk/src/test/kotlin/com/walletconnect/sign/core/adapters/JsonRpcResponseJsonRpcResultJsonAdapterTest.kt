package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.sign.common.adapters.JsonRpcResultAdapter
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName

internal class JsonRpcResponseJsonRpcResultJsonAdapterTest {

    @Test
    fun `test to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)
        val metadata = AppMetaData("name", "desc", "url", listOf("icon"))
        val approvalParams =
            SignParams.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124")
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
    fun `test from json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java)

        val metadata = AppMetaData("name", "desc", "url", listOf("icon"))
        val approvalParamsJsonResult = JsonRpcResponse.JsonRpcResult(id = 11L,
            result = SignParams.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124"))
        val resultString = moshi.adapter(JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)
        val result = adapter.fromJson(resultString)
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