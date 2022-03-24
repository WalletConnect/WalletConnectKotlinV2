package com.walletconnect.walletconnectv2.relay.model.mapper

import com.walletconnect.walletconnectv2.core.model.type.ClientParams
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionSettlementVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcHistoryVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.PendingRequestVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCResponseVO
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
internal fun RelayDO.JsonRpcResponse.JsonRpcError.toJsonRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
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