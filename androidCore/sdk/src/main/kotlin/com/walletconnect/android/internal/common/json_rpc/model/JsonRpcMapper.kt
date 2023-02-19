@file:JvmSynthetic

package com.walletconnect.android.internal.common.json_rpc.model

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.network.model.Relay

// TODO: This seems unnecessary, we converting JsonRpcResponse to JsonRpcResponse
@JvmSynthetic
internal fun JsonRpcResponse.toJsonRpcResponse(): JsonRpcResponse =
    when (this) {
        is JsonRpcResponse.JsonRpcResult -> toJsonRpcResult()
        is JsonRpcResponse.JsonRpcError -> toRpcError()
    }

// TODO: This seems unnecessary, we converting JsonRpcResult to JsonRpcResult
@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toJsonRpcResult(): JsonRpcResponse.JsonRpcResult =
    JsonRpcResponse.JsonRpcResult(id, result = result)

// TODO: This seems unnecessary, we converting JsonRpcError to JsonRpcError
@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

// TODO: This seems unnecessary, we converting JsonRpcError to JsonRpcError
@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toJsonRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toWCResponse(result: JsonRpcResponse, params: ClientParams): WCResponse =
    WCResponse(Topic(topic), method, result, params)

@JvmSynthetic
internal fun IrnParams.toRelay(): Relay.Model.IrnParams =
    Relay.Model.IrnParams(tag.id, ttl.seconds, prompt)