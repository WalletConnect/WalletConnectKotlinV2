package com.walletconnect.walletconnectv2.relay.model.mapper

import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.DeletedSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionNotificationVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionProposalVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionRequestVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.relay.model.RelayDO
import java.net.URI

internal fun JsonRpcResponseVO.toRelayDOJsonRpcResponse(): RelayDO.JsonRpcResponse =
    when (this) {
        is JsonRpcResponseVO.JsonRpcResult -> toRelayDOJsonRpcResult()
        is JsonRpcResponseVO.JsonRpcError -> toRelayDORpcError()
    }

internal fun JsonRpcResponseVO.JsonRpcResult.toRelayDOJsonRpcResult(): RelayDO.JsonRpcResponse.JsonRpcResult =
    RelayDO.JsonRpcResponse.JsonRpcResult(id, result)

internal fun JsonRpcResponseVO.JsonRpcError.toRelayDORpcError(): RelayDO.JsonRpcResponse.JsonRpcError =
    RelayDO.JsonRpcResponse.JsonRpcError(id, RelayDO.JsonRpcResponse.Error(error.code, error.message))

internal fun RelayDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result)