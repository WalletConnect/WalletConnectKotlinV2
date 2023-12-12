package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.util.Logger

internal class ApproveSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
//    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val cacaoVerifier: CacaoVerifier,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger,
    private val pairingController: PairingControllerInterface
) : ApproveSessionAuthenticateUseCaseInterface {
    override suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {

    }
}

internal interface ApproveSessionAuthenticateUseCaseInterface {

    suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}