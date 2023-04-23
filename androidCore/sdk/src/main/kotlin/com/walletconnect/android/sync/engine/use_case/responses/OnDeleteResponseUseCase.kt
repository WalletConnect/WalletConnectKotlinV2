package com.walletconnect.android.sync.engine.use_case.responses

import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.sync.common.json_rpc.SyncParams

internal class OnDeleteResponseUseCase() {
    suspend operator fun invoke(params: SyncParams.DeleteParams, response: WCResponse) {
        TODO()
        // Not yet sure if we should do anything on response
    }
}