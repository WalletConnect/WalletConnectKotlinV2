package com.walletconnect.sign.engine.use_case.responses

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionAuthenticateResponseUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val pairingController: PairingControllerInterface,
    private val pairingInterface: PairingInterface,
    private val crypto: KeyManagementRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: SignParams.SessionAuthenticateParams) = supervisorScope {

    }
}