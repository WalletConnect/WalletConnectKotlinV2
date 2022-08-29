package com.walletconnect.auth.json_rpc.model

import com.walletconnect.android_core.common.model.json_rpc.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.model.PendingRequestVO
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun AuthRpcDTO.AuthRequest.toPendingRequest(entry: JsonRpcHistory): PendingRequestVO =
    PendingRequestVO(
        entry.requestId,
        Topic(entry.topic),
        entry.method,
        params,
        entry.response
    )