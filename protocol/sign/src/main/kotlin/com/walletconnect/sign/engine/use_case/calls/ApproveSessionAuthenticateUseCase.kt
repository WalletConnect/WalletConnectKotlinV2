package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participant
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.MissingSessionAuthenticateRequest
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import kotlinx.coroutines.supervisorScope

internal class ApproveSessionAuthenticateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingSessionAuthenticateRequest: GetPendingSessionAuthenticateRequest,
    private val crypto: KeyManagementRepository,
    private val cacaoVerifier: CacaoVerifier,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger,
    private val pairingController: PairingControllerInterface,
    private val selfAppMetaData: AppMetaData,
) : ApproveSessionAuthenticateUseCaseInterface {
    override suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val jsonRpcHistoryEntry = getPendingSessionAuthenticateRequest(id)

        if (jsonRpcHistoryEntry == null) {
            logger.error(MissingSessionAuthenticateRequest().message)
            onFailure(MissingSessionAuthenticateRequest())
            return@supervisorScope
        }


        //todo: Add Expiry check
//        authParams.expiry?.let { expiry ->
//            if (checkExpiry(expiry, responseTopic, respond, authParams)) return@supervisorScope onFailure(InvalidExpiryException())
//        }


        val sessionAuthenticateParams: SignParams.SessionAuthenticateParams = jsonRpcHistoryEntry.params
        val receiverPublicKey = PublicKey(sessionAuthenticateParams.requester.publicKey)
        val senderPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)

        val irnParams = IrnParams(Tags.SESSION_AUTHENTICATE_RESPONSE, Ttl(DAY_IN_SECONDS))

        cacaos.find { cacao -> !cacaoVerifier.verify(cacao) }?.also{
            logger.error("Invalid Cacao")
            //todo: handle error codes
            jsonRpcInteractor.respondWithError(id,
                responseTopic,
                PeerError.EIP1193.UserRejectedRequest("Invalid CACAO"),
                irnParams,
                EnvelopeType.ONE,
                Participants(senderPublicKey, receiverPublicKey),
                onSuccess = {
                    return@respondWithError onFailure(Throwable("Error successfully sent on topic: $responseTopic"))
                },
                onFailure = {
                    logger.error("Error failure on topic: $responseTopic")
                })
            return@supervisorScope
        }

        val responseParams = CoreSignParams.SessionAuthenticateApproveParams(
            responder = Participant(
                publicKey = senderPublicKey.keyAsHex,
                metadata = selfAppMetaData
            ),
            caip222Response = cacaos
        )

        val response: JsonRpcResponse = JsonRpcResponse.JsonRpcResult(id, result = responseParams)

        crypto.setKey(symmetricKey, responseTopic.value)

        jsonRpcInteractor.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = {
                logger.log("Success Responded on topic: $responseTopic")
//                scope.launch {
//                    supervisorScope {
//                        pairingController.activate(Core.Params.Activate(jsonRpcHistoryEntry.topic.value))
//                        verifyContextStorageRepository.delete(id)
//                    }
//                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Error Responded on topic: $responseTopic")
//                scope.launch {
//                    supervisorScope {
//                        verifyContextStorageRepository.delete(respond.id)
//                    }
//                }
                onFailure(error)
            }
        )
    }
}

internal interface ApproveSessionAuthenticateUseCaseInterface {

    suspend fun approveSessionAuthenticate(id: Long, cacaos: List<Cacao>, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}