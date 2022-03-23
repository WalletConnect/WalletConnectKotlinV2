package com.walletconnect.walletconnectv2.core.adapters

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AgreementPeer
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.jvmName

internal class RelayDOJsonRpcResponseJsonRpcResultJsonAdapterTest {

    @Test
    fun `test to json`() {
        val moshi = Moshi.Builder().add { type, _, moshi ->
            return@add if (type.getRawType().name == RelayDO.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                RelayDOJsonRpcResultJsonAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(RelayDO.JsonRpcResponse.JsonRpcResult::class.java)
        val approvalParams =
            SessionParamsVO.ApprovalParams(relay = RelayProtocolOptionsVO("waku"), responder = AgreementPeer(publicKey = "124"))
        val jsonResult = RelayDO.JsonRpcResponse.JsonRpcResult(
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
            return@add if (type.getRawType().name == RelayDO.JsonRpcResponse.JsonRpcResult::class.jvmName) {
                RelayDOJsonRpcResultJsonAdapter(moshi = moshi)
            } else {
                null
            }
        }.build()
        val adapter = moshi.adapter(RelayDO.JsonRpcResponse.JsonRpcResult::class.java)

        val approvalParamsJsonResult = RelayDO.JsonRpcResponse.JsonRpcResult(id = 11L,
            result = SessionParamsVO.ApprovalParams(relay = RelayProtocolOptionsVO("waku"), responder = AgreementPeer(publicKey = "124")))
        val resultString = moshi.adapter(RelayDO.JsonRpcResponse.JsonRpcResult::class.java).toJson(approvalParamsJsonResult)
        val result = adapter.fromJson(resultString)
        result is RelayDO.JsonRpcResponse.JsonRpcResult

        println()
        println(result)
        println()
    }
}