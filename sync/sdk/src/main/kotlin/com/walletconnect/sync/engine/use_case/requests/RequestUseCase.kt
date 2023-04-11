package com.walletconnect.sync.engine.use_case.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.sync.common.json_rpc.SyncParams

internal interface RequestUseCase<P : SyncParams> {
    suspend operator fun invoke(params: P, request: WCRequest)
}