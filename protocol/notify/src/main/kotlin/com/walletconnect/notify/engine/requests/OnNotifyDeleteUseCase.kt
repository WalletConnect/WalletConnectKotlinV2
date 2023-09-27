@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyDeleteUseCase(

    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, requestParams: CoreNotifyParams.DeleteParams) = supervisorScope {
        logger.error("onNotifyDelete: $request")
        // TODO: Remove this method. It's triggered when other clients remove subscription. We shouldn't trust this method but wait for state change from wc_notifySubscriptionsChanged
    }
}