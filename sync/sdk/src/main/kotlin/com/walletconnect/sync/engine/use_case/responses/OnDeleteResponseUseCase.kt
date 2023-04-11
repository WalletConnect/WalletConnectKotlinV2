package com.walletconnect.sync.engine.use_case.responses

import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.sync.common.json_rpc.SyncParams

internal class OnDeleteResponseUseCase() :
    ResponseUseCase<SyncParams.DeleteParams> {
    override suspend fun invoke(params: SyncParams.DeleteParams, response: WCResponse) {
        TODO()
        // Not yet sure if we should do anything on response
    }
}