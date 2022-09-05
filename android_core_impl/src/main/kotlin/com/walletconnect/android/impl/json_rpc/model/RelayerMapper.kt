@file:JvmSynthetic

package com.walletconnect.android.impl.json_rpc.model

import com.walletconnect.android.api.JsonRpc
import com.walletconnect.android.impl.common.model.IrnParams
import com.walletconnect.android.impl.common.model.json_rpc.JsonRpcHistory
import com.walletconnect.android.impl.common.model.json_rpc.JsonRpcResponse
import com.walletconnect.android.impl.common.model.sync.WCResponse
import com.walletconnect.android.impl.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcResponse.toJsonRpcResponse(): JsonRpc.JsonRpcResponse =
    when (this) {
        is JsonRpcResponse.JsonRpcResult -> toJsonRpcResult()
        is JsonRpcResponse.JsonRpcError -> toRpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toJsonRpcResult(): JsonRpc.JsonRpcResponse.JsonRpcResult =
    JsonRpc.JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toRpcError(): JsonRpc.JsonRpcResponse.JsonRpcError =
    JsonRpc.JsonRpcResponse.JsonRpcError(id, error = JsonRpc.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpc.JsonRpcResponse.JsonRpcError.toJsonRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistory.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)