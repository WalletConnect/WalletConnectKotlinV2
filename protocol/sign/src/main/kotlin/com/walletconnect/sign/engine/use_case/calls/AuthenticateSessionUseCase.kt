package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.engine.model.EngineDO

internal class AuthenticateSessionUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) : AuthenticateSessionUseCaseInterface {
    override suspend fun authenticate(payloadParams: EngineDO.PayloadParams, topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {

    }
}

internal interface AuthenticateSessionUseCaseInterface {
    suspend fun authenticate(payloadParams: EngineDO.PayloadParams, topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}