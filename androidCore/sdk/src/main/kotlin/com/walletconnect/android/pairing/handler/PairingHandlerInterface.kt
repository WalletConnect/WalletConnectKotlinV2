package com.walletconnect.android.pairing.handler

import com.walletconnect.android.Core
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PairingHandlerInterface {
    val topicExpiredFlow: SharedFlow<Topic>
    val findWrongMethodsFlow: Flow<InternalError>

    fun activate(activate: Core.Params.Activate, onError: (Core.Model.Error) -> Unit = {})

    fun updateExpiry(updateExpiry: Core.Params.UpdateExpiry, onError: (Core.Model.Error) -> Unit = {})

    fun updateMetadata(updateMetadata: Core.Params.UpdateMetadata, onError: (Core.Model.Error) -> Unit = {})

    fun register(vararg method: String)
}