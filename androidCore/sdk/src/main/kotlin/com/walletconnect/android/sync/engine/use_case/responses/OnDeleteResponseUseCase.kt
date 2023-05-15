package com.walletconnect.android.sync.engine.use_case.responses

import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.foundation.util.Logger

internal class OnDeleteResponseUseCase(
    private val logger: Logger,
) {
    suspend operator fun invoke(params: SyncParams.DeleteParams, response: WCResponse) {
        // Not yet sure if we should do anything on response
        logger.log("Received response: $response, params: $params")
    }
}