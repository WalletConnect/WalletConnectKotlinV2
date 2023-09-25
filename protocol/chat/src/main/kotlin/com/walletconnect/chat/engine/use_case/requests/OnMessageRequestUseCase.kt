package com.walletconnect.chat.engine.use_case.requests

import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.InvalidActClaims
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.model.ChatMessage
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.common.model.Message
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.jwt.use_case.EncodeChatReceiptDidJwtPayloadUseCase
import com.walletconnect.chat.storage.MessageStorageRepository
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnMessageRequestUseCase(
    private val logger: Logger,
    private val identitiesInteractor: IdentitiesInteractor,
    private val messageRepository: MessageStorageRepository,
    private val keyserverUrl: String,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcRequest: WCRequest, params: ChatParams.MessageParams) {
        val claims = extractVerifiedDidJwtClaims<ChatDidJwtClaims.ChatMessage>(params.messageAuth).getOrElse() { error -> return@invoke logger.error(error) }
        if (claims.action != ChatDidJwtClaims.ChatMessage.ACT) return logger.error(InvalidActClaims(ChatDidJwtClaims.ChatMessage.ACT))

        scope.launch {
            val authorAccountId = identitiesInteractor.resolveIdentityDidKey(claims.issuer)
                .getOrElse() { error -> return@launch logger.error(error) }

            val message = Message(wcRequest.id, wcRequest.topic, ChatMessage(claims.subject), authorAccountId, claims.issuedAt, claims.media)
            messageRepository.insertMessage(message)
            _events.emit(Events.OnMessage(message))

            // With sync integration we need to check if I'm the author by checking if I have identity keys for the account
            // If they exists it means I authored the message from other client
            // If they don't exists it means someone sent me the message
            val recipientAccountId = AccountId(decodeDidPkh(claims.audience))

            if (shouldClientRespondToRequest(authorAccountId, recipientAccountId)) {
                // Currently timestamps are based on claims issuedAt. Which MUST be changed to achieve proper order of messages.
                // Should be changed with specs: https://github.com/WalletConnect/walletconnect-docs/pull/473.
                // Change: Instead of claims.issuedAt use wcRequest.receivedAt

                val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(recipientAccountId)

                val didJwt = encodeDidJwt(
                    identityPrivateKey,
                    EncodeChatReceiptDidJwtPayloadUseCase(claims.subject, authorAccountId),
                    EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
                ).getOrElse() { error -> return@launch logger.error(error) }

                val receiptParams = CoreChatParams.ReceiptParams(receiptAuth = didJwt.value)
                val irnParams = IrnParams(Tags.CHAT_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))

                jsonRpcInteractor.respondWithParams(wcRequest, receiptParams, irnParams, EnvelopeType.ZERO) { error -> logger.error(error) }
            }
        }
    }

    /**
     * Rare case when a device has clients Ax and Bx registered and needs to handle messages between them.
     * Context: When I sent a message from A1 client, it was impossible to distinct if I should respond to message with sync,
     * as I received message request on A2 and Bx clients
     * Client doesn't send the response only when it has author keys, if it has both author and recipient then it does send the response
     */
    private fun shouldClientRespondToRequest(authorAccountId: AccountId, recipientAccountId: AccountId): Boolean {
        return runCatching { identitiesInteractor.getIdentityKeyPair(authorAccountId) }
            .getOrNull()?.let {
                // If author account is registered check if recipient is also registered
                runCatching { identitiesInteractor.getIdentityKeyPair(recipientAccountId) }.getOrNull()?.let {
                    true // If both author account and recipient account are registered respond to request
                } ?: false // If only author account is register do not respond to request
            } ?: true // If author account is not registered respond to request
    }
}