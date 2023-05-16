package com.walletconnect.chat.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.foundation.util.Logger
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

internal class OnMessageResponseUseCase(
    private val logger: Logger,
) {
    operator fun invoke(wcResponse: WCResponse) {
        TimeUnit.SECONDS.toMillis(THIRTY_SECONDS)
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> logger.log("Message error response: $response")
            is JsonRpcResponse.JsonRpcResult -> logger.log("Message success response")
            //todo on result validate receipt auth and notify that message was received. Needs discussion and specs (Milestone 2)
        }
    }
}