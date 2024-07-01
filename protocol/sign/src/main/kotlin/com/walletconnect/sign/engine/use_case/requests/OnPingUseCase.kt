package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.thirtySeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.supervisorScope

internal class OnPingUseCase(private val jsonRpcInteractor: RelayJsonRpcInteractorInterface, private val logger: Logger) {

    suspend operator fun invoke(request: WCRequest) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(thirtySeconds))
        logger.log("Session ping received on topic: ${request.topic}")
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }
}