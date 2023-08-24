package com.walletconnect.auth.json_rpc.model

import com.walletconnect.android.internal.common.json_rpc.model.JsonRpcHistoryRecord
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

@JvmSynthetic
internal fun AuthParams.RequestParams.toEntry(record: JsonRpcHistoryRecord): JsonRpcHistoryEntry =
    JsonRpcHistoryEntry(
        record.id,
        Topic(record.topic),
        record.method,
        this,
        record.response
    )