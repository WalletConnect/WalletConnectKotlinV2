package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.exception.Invalid
import com.walletconnect.android.internal.common.exception.InvalidExpiryException
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.CoreAuthParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.storage.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.exceptions.InvalidCacaoException
import com.walletconnect.auth.common.exceptions.MissingAuthRequestException
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.auth.common.model.Respond
import com.walletconnect.auth.engine.mapper.toCacaoPayload
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RespondAuthRequestUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val crypto: KeyManagementRepository,
    private val cacaoVerifier: CacaoVerifier,
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val logger: Logger,
    private val pairingController: PairingControllerInterface
) : RespondAuthRequestUseCaseInterface {

    override suspend fun respond(respond: Respond, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(respond.id)

        if (jsonRpcHistoryEntry == null) {
            logger.error(MissingAuthRequestException.message)
            onFailure(MissingAuthRequestException)
            return@supervisorScope
        }

        val authParams: AuthParams.RequestParams = jsonRpcHistoryEntry.params
        val response: JsonRpcResponse = handleResponse(respond, authParams)
        val receiverPublicKey = PublicKey(authParams.requester.publicKey)
        val senderPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
        val symmetricKey: SymmetricKey = crypto.generateSymmetricKeyFromKeyAgreement(senderPublicKey, receiverPublicKey)
        val responseTopic: Topic = crypto.getTopicFromKey(receiverPublicKey)

        authParams.expiry?.let { expiry ->
            if (checkExpiry(expiry, responseTopic, respond, authParams)) return@supervisorScope onFailure(InvalidExpiryException())
        }

        crypto.setKey(symmetricKey, responseTopic.value)
        val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS), false)
        jsonRpcInteractor.publishJsonRpcResponse(
            responseTopic, irnParams, response, envelopeType = EnvelopeType.ONE, participants = Participants(senderPublicKey, receiverPublicKey),
            onSuccess = {
                logger.log("Success Responded on topic: $responseTopic")
                scope.launch {
                    supervisorScope {
                        pairingController.activate(Core.Params.Activate(jsonRpcHistoryEntry.topic.value)) //todo: check if pairing topic
                        verifyContextStorageRepository.delete(respond.id)
                    }
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Error Responded on topic: $responseTopic")
                scope.launch {
                    supervisorScope {
                        verifyContextStorageRepository.delete(respond.id)
                    }
                }
                onFailure(error)
            }
        )
    }

    private fun checkExpiry(expiry: Expiry, responseTopic: Topic, respond: Respond, authParams: AuthParams.RequestParams): Boolean {
        if (!CoreValidator.isExpiryWithinBounds(expiry)) {
            val irnParams = IrnParams(Tags.AUTH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))
            val wcRequest = WCRequest(responseTopic, respond.id, JsonRpcMethod.WC_AUTH_REQUEST, authParams)
            jsonRpcInteractor.respondWithError(wcRequest, Invalid.RequestExpired, irnParams)
            return true
        }
        return false
    }

    private fun handleResponse(respond: Respond, authParams: AuthParams.RequestParams) = when (respond) {
        is Respond.Error -> JsonRpcResponse.JsonRpcError(respond.id, error = JsonRpcResponse.Error(respond.code, respond.message))
        is Respond.Result -> {
            val issuer = Issuer(respond.iss)
            val payload: Cacao.Payload = authParams.payloadParams.toCacaoPayload(issuer)
            val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, respond.signature.toCommon())
            val responseParams = CoreAuthParams.ResponseParams(cacao.header, cacao.payload, cacao.signature)
            if (!cacaoVerifier.verify(cacao)) throw InvalidCacaoException
            JsonRpcResponse.JsonRpcResult(respond.id, result = responseParams)
        }
    }
}

internal interface RespondAuthRequestUseCaseInterface {
    suspend fun respond(respond: Respond, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}