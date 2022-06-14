package com.walletconnect.sign.relay.model

import com.walletconnect.sign.core.model.type.ClientParams
import com.walletconnect.sign.core.model.vo.TopicVO
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcHistoryVO
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.sign.core.model.vo.sync.PendingRequestVO
import com.walletconnect.sign.core.model.vo.sync.WCResponseVO

@JvmSynthetic
internal fun JsonRpcResponseVO.toRelayerDOJsonRpcResponse(): RelayerDO.JsonRpcResponse =
    when (this) {
        is JsonRpcResponseVO.JsonRpcResult -> toRelayerDOJsonRpcResult()
        is JsonRpcResponseVO.JsonRpcError -> toRelayerDORpcError()
    }

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toRelayerDOJsonRpcResult(): RelayerDO.JsonRpcResponse.JsonRpcResult =
    RelayerDO.JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toRelayerDORpcError(): RelayerDO.JsonRpcResponse.JsonRpcError =
    RelayerDO.JsonRpcResponse.JsonRpcError(id, error = RelayerDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun RelayerDO.JsonRpcResponse.JsonRpcError.toJsonRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(error.code, error.message))

@JvmSynthetic
internal fun JsonRpcHistoryVO.toWCResponse(result: JsonRpcResponseVO, params: ClientParams): WCResponseVO =
    WCResponseVO(TopicVO(topic), method, result, params)

@JvmSynthetic
internal fun SessionSettlementVO.SessionRequest.toPendingRequestVO(entry: JsonRpcHistoryVO): PendingRequestVO =
    PendingRequestVO(
        entry.requestId,
        entry.topic,
        params.request.method,
        params.chainId,
        params.request.params.toString(),
    )