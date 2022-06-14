package com.walletconnect.sign.core.adapters

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.sign.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.sign.relay.model.RelayerDO
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName

internal class RelayerDOJsonRpcResponseJsonRpcResultJsonAdapterTest {

    @Test
    fun `test to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == RelayerDO.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                RelayDOJsonRpcResultJsonAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java)
        val metadata = MetaDataVO("name", "desc", "url", listOf("icon"))
        val approvalParams =
            SessionParamsVO.ApprovalParams(relay = RelayProtocolOptionsVO("waku"), responderPublicKey = "124")
        val jsonResult = RelayerDO.JsonRpcResponse.JsonRpcResult(
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
            return@add if (type.getRawType().name == RelayerDO.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                RelayDOJsonRpcResultJsonAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java)

        val metadata = MetaDataVO("name", "desc", "url", listOf("icon"))
        val approvalParamsJsonResult = RelayerDO.JsonRpcResponse.JsonRpcResult(id = 11L,
            result = SessionParamsVO.ApprovalParams(relay = RelayProtocolOptionsVO("waku"), responderPublicKey = "124"))
        val resultString = moshi.adapter(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)
        val result = adapter.fromJson(resultString)
        result is RelayerDO.JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }

    @Test
    fun `test from json with boolean`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == RelayerDO.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                RelayDOJsonRpcResultJsonAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java)

        val approvalParamsJsonResult = RelayerDO.JsonRpcResponse.JsonRpcResult(id = 11L, result = true)
        val resultString = moshi.adapter(RelayerDO.JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)

        val result = adapter.fromJson(resultString)
        result is RelayerDO.JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }
}