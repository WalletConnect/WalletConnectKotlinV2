package com.walletconnect.walletconnectv2.relay.model.mapper

import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.relay.model.RelayDO

internal fun JsonRpcResponseVO.toRelayDOJsonRpcResponse(): RelayDO.JsonRpcResponse =
    when (this) {
        is JsonRpcResponseVO.JsonRpcResult -> toRelayDOJsonRpcResult()
        is JsonRpcResponseVO.JsonRpcError -> toRelayDORpcError()
    }

internal fun JsonRpcResponseVO.JsonRpcResult.toRelayDOJsonRpcResult(): RelayDO.JsonRpcResponse.JsonRpcResult =
    RelayDO.JsonRpcResponse.JsonRpcResult(id, result = result)

internal fun JsonRpcResponseVO.JsonRpcError.toRelayDORpcError(): RelayDO.JsonRpcResponse.JsonRpcError =
    RelayDO.JsonRpcResponse.JsonRpcError(id, error = RelayDO.JsonRpcResponse.Error(error.code, error.message))

internal fun RelayDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result.toString())