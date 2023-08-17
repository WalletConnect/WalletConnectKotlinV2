package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.THIRTY_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import kotlinx.coroutines.supervisorScope

internal class OnPingUseCase(private val jsonRpcInteractor: JsonRpcInteractorInterface) {

    suspend operator fun invoke(request: WCRequest) = supervisorScope {
        val irnParams = IrnParams(Tags.SESSION_PING_RESPONSE, Ttl(THIRTY_SECONDS))
        jsonRpcInteractor.respondWithSuccess(request, irnParams)
    }
}