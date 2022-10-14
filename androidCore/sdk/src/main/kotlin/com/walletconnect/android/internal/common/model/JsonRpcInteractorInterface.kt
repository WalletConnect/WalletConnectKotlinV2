package com.walletconnect.android.internal.common.model

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.exception.WalletConnectException
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface JsonRpcInteractorInterface {
    val clientSyncJsonRpc: SharedFlow<WCRequest>
    val peerResponse: SharedFlow<WCResponse>
    val initializationErrorsFlow: Flow<WalletConnectException>
    val isConnectionAvailable: StateFlow<Boolean>
    val internalErrors: SharedFlow<InternalError>

    fun checkConnectionWorking()

    fun subscribe(topic: Topic)

    fun unsubscribe(topic: Topic)

    fun publishJsonRpcRequests(
        topic: Topic,
        params: IrnParams,
        payload: JsonRpcClientSync<*>,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    )

    fun publishJsonRpcResponse(
        topic: Topic,
        params: IrnParams,
        response: JsonRpcResponse,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        participants: Participants? = null,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
    )

    fun respondWithParams(
        request: WCRequest,
        clientParams: ClientParams,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
    )

    fun respondWithSuccess(
        request: WCRequest,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
    )

    fun respondWithError(
        request: WCRequest,
        error: Error,
        irnParams: IrnParams,
        envelopeType: EnvelopeType = EnvelopeType.ZERO,
        participants: Participants? = null,
        onFailure: (Throwable) -> Unit = {},
    )
}