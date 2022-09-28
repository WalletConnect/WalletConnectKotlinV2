@file:JvmSynthetic

package com.walletconnect.android.common.json_rpc

import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.model.IrnParams
import com.walletconnect.android.common.model.WCResponse
import com.walletconnect.android.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

@JvmSynthetic
internal fun JsonRpcResponse.toJsonRpcResponse(): JsonRpcResponse =
    when (this) {
        is JsonRpcResponse.JsonRpcResult -> toJsonRpcResult()
        is JsonRpcResponse.JsonRpcError -> toRpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toJsonRpcResult(): JsonRpcResponse.JsonRpcResult =
    JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toJsonRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)