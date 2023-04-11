package com.walletconnect.sync.engine.use_case.responses

import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.sync.common.json_rpc.SyncParams

internal interface ResponseUseCase<P : SyncParams> {
    suspend operator fun invoke(params: P, response: WCResponse)
}