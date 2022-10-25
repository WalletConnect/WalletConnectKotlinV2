@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.model

import com.walletconnect.android.impl.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpcVO

@JvmSynthetic
internal fun SignRpcVO.SessionRequest.toPendingRequest(entry: JsonRpcHistoryRecord): PendingRequest =
    PendingRequest(
        entry.id,
        Topic(entry.topic),
        params.request.method,
        params.chainId,
        params.request.params,
    )