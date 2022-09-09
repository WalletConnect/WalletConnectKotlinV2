package com.walletconnect.auth.json_rpc.model

import com.walletconnect.android.impl.json_rpc.model.JsonRpcHistoryRecord
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.model.JsonRpcHistoryEntry
import com.walletconnect.foundation.common.model.Topic

@JvmSynthetic
internal fun JsonRpcHistoryRecord.toEntry(params: AuthParams.RequestParams): JsonRpcHistoryEntry =
    JsonRpcHistoryEntry(
        id,
        Topic(topic),
        method,
        params,
        response
    )