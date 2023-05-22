package com.walletconnect.chat.engine.use_case.requests

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.AccountsAlreadyHaveInviteException
import com.walletconnect.chat.common.exceptions.InvalidActClaims
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteMessage
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.engine.sync.use_case.requests.SetReceivedInviteStatusToChatSentInvitesStoreUseCase
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnInviteRequestUseCase(
    private val logger: Logger,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val setReceivedInviteStatusToChatSentInvitesStoreUseCase: SetReceivedInviteStatusToChatSentInvitesStoreUseCase,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    operator fun invoke(wcRequest: WCRequest, params: ChatParams.InviteParams) {
        val claims = extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteProposal>(params.inviteAuth)
            .getOrElse() { error ->
                logger.error(error)
                return@invoke
            }
        if (claims.action != ChatDidJwtClaims.InviteProposal.ACT) return logger.error(InvalidActClaims(ChatDidJwtClaims.InviteProposal.ACT))

        scope.launch {
            val inviterAccountId = identitiesInteractor.resolveIdentityDidKey(claims.issuer)
                .getOrElse() { error ->
                    logger.error(error)
                    return@launch
                }
            logger.log("Invite received. Resolved identity: $inviterAccountId")

            runCatching { accountsRepository.getAccountByInviteTopic(wcRequest.topic) }.fold(onSuccess = { inviteeAccount ->
                if (invitesRepository.checkIfAccountsHaveExistingInvite(inviterAccountId.value, inviteeAccount.accountId.value)) {
                    logger.error(AccountsAlreadyHaveInviteException)
                    return@launch
                }

                val inviteePublicKey = inviteeAccount.publicInviteKey ?: throw Throwable("Missing publicInviteKey")
                val inviterPublicKey = decodeX25519DidKey(claims.inviterPublicKey)
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                val invite = Invite.Received(
                    wcRequest.id, inviterAccountId, inviteeAccount.accountId, InviteMessage(claims.subject),
                    inviterPublicKey, InviteStatus.PENDING, acceptTopic, symmetricKey, inviterPrivateKey = null,
                    //todo: use publishedAt from relay https://github.com/WalletConnect/WalletConnectKotlinV2/issues/872
                    timestamp = wcRequest.id
                )

                setReceivedInviteStatusToChatSentInvitesStoreUseCase(invite, onSuccess = {}, onError = { error -> scope.launch { _events.emit(SDKError(error)) } })
                invitesRepository.insertInvite(invite)
                _events.emit(Events.OnInvite(invite))
            }, onFailure = { error -> logger.error(error) })
        }
    }
}