@file:JvmSynthetic

package com.walletconnect.sign.json_rpc.model

import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams

@JvmSynthetic
internal fun SignRpc.SessionRequest.toPendingRequest(entry: JsonRpcHistoryRecord): PendingRequest<String> =
    PendingRequest(
        entry.id,
        Topic(entry.topic),
        params.request.method,
        params.chainId,
        params.request.params,
        params.request.expiry
    )

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toPending(params: SignParams.SessionRequestParams): PendingRequest<SignParams.SessionRequestParams> =
    PendingRequest(
        id,
        Topic(topic),
        method,
        params.chainId,
        params,
    )