@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.model

import com.walletconnect.android_core.common.model.json_rpc.JsonRpcHistory
import com.walletconnect.android_core.common.model.sync.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO

@JvmSynthetic
internal fun SessionRpcVO.SessionRequest.toPendingRequestVO(entry: JsonRpcHistory): PendingRequest =
    PendingRequest(
        entry.requestId,
        entry.topic,
        params.request.method,
        params.chainId,
        params.request.params,
    )