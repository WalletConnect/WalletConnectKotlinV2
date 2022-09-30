package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.sign.common.adapters.JsonRpcResultAdapter
import com.walletconnect.android.common.model.RelayProtocolOptions
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SessionParamsVO
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName

internal class JsonRpcResponseJsonRpcResultJsonAdapterTest {

    @Test
    fun `test to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.java)
        val metadata = MetaData("name", "desc", "url", listOf("icon"))
        val approvalParams =
            SessionParamsVO.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124")
        val jsonResult = com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult(
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
            return@add if (type.getRawType().name == com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.java)

        val metadata = MetaData("name", "desc", "url", listOf("icon"))
        val approvalParamsJsonResult = com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult(id = 11L,
            result = SessionParamsVO.ApprovalParams(relay = RelayProtocolOptions("irn"), responderPublicKey = "124"))
        val resultString = moshi.adapter(com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)
        val result = adapter.fromJson(resultString)
        result is com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }

    @Test
    fun `test from json with boolean`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                JsonRpcResultAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.java)

        val approvalParamsJsonResult = com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult(id = 11L, result = true)
        val resultString = moshi.adapter(com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)

        val result = adapter.fromJson(resultString)
        result is com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }
}