package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest

internal class RejectSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest,
    private val crypto: KeyManagementRepository,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger
) : RejectSessionAuthenticateUseCaseInterface {
    override suspend fun rejectSessionAuthenticate(id: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {

    }
}

internal interface RejectSessionAuthenticateUseCaseInterface {
    suspend fun rejectSessionAuthenticate(id: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}