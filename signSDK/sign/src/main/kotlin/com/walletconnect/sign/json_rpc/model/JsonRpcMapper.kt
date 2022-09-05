@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.model

import com.walletconnect.android_core.common.model.json_rpc.JsonRpcHistoryRecord
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO

@JvmSynthetic
internal fun SessionRpcVO.SessionRequest.toPendingRequest(entry: JsonRpcHistoryRecord): PendingRequest =
    PendingRequest(
        entry.id,
        Topic(entry.topic),
        params.request.method,
        params.chainId,
        params.request.params,
    )