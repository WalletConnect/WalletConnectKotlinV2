package com.walletconnect.sync.engine.use_case.responses

import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.sync.common.json_rpc.SyncParams

internal class OnSetResponseUseCase() :
    ResponseUseCase<SyncParams.SetParams> {
    override suspend fun invoke(params: SyncParams.SetParams, response: WCResponse) {
        TODO()
        // Not yet sure if we should do anything on response
    }
}