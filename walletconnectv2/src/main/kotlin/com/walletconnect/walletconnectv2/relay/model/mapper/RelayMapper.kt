package com.walletconnect.walletconnectv2.relay.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.relay.model.RelayDO

@JvmSynthetic
internal fun JsonRpcResponseVO.toRelayDOJsonRpcResponse(): RelayDO.JsonRpcResponse =
    when (this) {
        is JsonRpcResponseVO.JsonRpcResult -> toRelayDOJsonRpcResult()
        is JsonRpcResponseVO.JsonRpcError -> toRelayDORpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toRelayDOJsonRpcResult(): RelayDO.JsonRpcResponse.JsonRpcResult =
    RelayDO.JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toRelayDORpcError(): RelayDO.JsonRpcResponse.JsonRpcError =
    RelayDO.JsonRpcResponse.JsonRpcError(id, error = RelayDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun RelayDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result.toString())

@JvmSynthetic
internal fun RelayDO.JsonRpcResponse.JsonRpcError.toJsonRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(error.code, error.message))