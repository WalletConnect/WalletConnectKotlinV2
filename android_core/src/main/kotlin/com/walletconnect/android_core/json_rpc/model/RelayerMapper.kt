package com.walletconnect.android_core.json_rpc.model

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.vo.IrnParamsVO
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcHistoryVO
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcResponseVO
import com.walletconnect.android_core.common.model.vo.sync.WCResponseVO
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcResponseVO.toRelayerDOJsonRpcResponse(): JsonRpc.JsonRpcResponse =
    when (this) {
        is JsonRpcResponseVO.JsonRpcResult -> toRelayerDOJsonRpcResult()
        is JsonRpcResponseVO.JsonRpcError -> toRelayerDORpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toRelayerDOJsonRpcResult(): JsonRpc.JsonRpcResponse.JsonRpcResult =
    JsonRpc.JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toRelayerDORpcError(): JsonRpc.JsonRpcResponse.JsonRpcError =
    JsonRpc.JsonRpcResponse.JsonRpcError(id, error = JsonRpc.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpc.JsonRpcResponse.JsonRpcError.toJsonRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistoryVO.toWCResponse(result: JsonRpcResponseVO, params: ClientParams): WCResponseVO =
    WCResponseVO(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParamsVO.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)