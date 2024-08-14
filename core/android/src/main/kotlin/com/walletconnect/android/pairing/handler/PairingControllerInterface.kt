package com.walletconnect.android.pairing.handler

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PairingControllerInterface {
    val findWrongMethodsFlow: Flow<SDKError>
    val storedPairingFlow: SharedFlow<Pair<Topic, MutableList<String>>>
    val checkVerifyKeyFlow: SharedFlow<Unit>

    fun initialize()

    fun setRequestReceived(activate: Core.Params.RequestReceived, onError: (Core.Model.Error) -> Unit = {})

    fun updateMetadata(updateMetadata: Core.Params.UpdateMetadata, onError: (Core.Model.Error) -> Unit = {})

    fun deleteAndUnsubscribePairing(deletePairing: Core.Params.Delete, onError: (Core.Model.Error) -> Unit = {})

    fun register(vararg method: String)

    fun getPairingByTopic(topic: Topic): Pairing?
}