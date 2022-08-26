package com.walletconnect.auth.json_rpc.model

import com.walletconnect.android_core.common.model.json_rpc.JsonRpcHistory
import com.walletconnect.auth.common.json_rpc.AuthRpcDTO
import com.walletconnect.auth.common.model.PendingRequest

@JvmSynthetic
internal fun AuthRpcDTO.AuthRequest.toEntry(entry: JsonRpcHistory): PendingRequest =
    PendingRequest(
        entry.requestId,
        params.payloadParams,
        entry.response
    )