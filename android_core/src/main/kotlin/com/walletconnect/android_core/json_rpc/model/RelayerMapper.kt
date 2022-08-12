package com.walletconnect.android_core.json_rpc.model

import com.walletconnect.android_core.common.model.type.ClientParams
import com.walletconnect.android_core.common.model.vo.IrnParams
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcHistoryVO
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcResponse
import com.walletconnect.android_core.common.model.vo.sync.WCResponse
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcResponse.toRelayerDOJsonRpcResponse(): JsonRpc.JsonRpcResponse =
    when (this) {
        is JsonRpcResponse.JsonRpcResult -> toRelayerDOJsonRpcResult()
        is JsonRpcResponse.JsonRpcError -> toRelayerDORpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toRelayerDOJsonRpcResult(): JsonRpc.JsonRpcResponse.JsonRpcResult =
    JsonRpc.JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toRelayerDORpcError(): JsonRpc.JsonRpcResponse.JsonRpcError =
    JsonRpc.JsonRpcResponse.JsonRpcError(id, error = JsonRpc.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpc.JsonRpcResponse.JsonRpcError.toJsonRpcErrorVO(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistoryVO.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)