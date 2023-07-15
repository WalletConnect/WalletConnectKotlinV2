package com.walletconnect.chat.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.foundation.util.Logger

internal class OnLeaveResponseUseCase(
    private val logger: Logger,
) {
    operator fun invoke(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> logger.log("Chat leave error response: $response")
            is JsonRpcResponse.JsonRpcResult -> logger.log("Chat leave success response")
        }
    }
}