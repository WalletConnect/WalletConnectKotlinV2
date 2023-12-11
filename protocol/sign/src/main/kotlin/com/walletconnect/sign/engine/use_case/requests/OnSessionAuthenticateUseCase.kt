package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val resolveAttestationIdUseCase: ResolveAttestationIdUseCase,
    private val pairingController: PairingControllerInterface
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, payloadParams: SignParams.SessionAuthenticateParams) = supervisorScope {

    }
}