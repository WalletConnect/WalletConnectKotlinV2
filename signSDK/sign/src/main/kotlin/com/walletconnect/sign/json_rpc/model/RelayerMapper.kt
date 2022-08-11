package com.walletconnect.sign.json_rpc.model

import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcHistoryVO
import com.walletconnect.android_core.common.model.vo.sync.PendingRequestVO
import com.walletconnect.sign.core.model.vo.clientsync.session.SessionRpcVO

@JvmSynthetic
internal fun SessionRpcVO.SessionRequest.toPendingRequestVO(entry: JsonRpcHistoryVO): PendingRequestVO =
    PendingRequestVO(
        entry.requestId,
        entry.topic,
        params.request.method,
        params.chainId,
        params.request.params,
    )