package com.walletconnect.chat.engine.use_case.requests

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.AccountsAlreadyHaveInviteException
import com.walletconnect.chat.common.exceptions.AccountsAlreadyHaveThreadException
import com.walletconnect.chat.common.exceptions.InvalidActClaims
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteMessage
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import com.walletconnect.utils.extractTimestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class OnInviteRequestUseCase(
    private val logger: Logger,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val invitesRepository: InvitesStorageRepository,
    private val threadsRepository: ThreadsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcRequest: WCRequest, params: ChatParams.InviteParams) {
        val claims = extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteProposal>(params.inviteAuth).getOrElse() { error -> return logger.error(error) }
        if (claims.action != ChatDidJwtClaims.InviteProposal.ACT) return logger.error(InvalidActClaims(ChatDidJwtClaims.InviteProposal.ACT))

        val inviterAccountId = identitiesInteractor.resolveIdentityDidKey(claims.issuer).getOrElse() { error -> return logger.error(error) }

        runCatching { accountsRepository.getAccountByInviteTopic(wcRequest.topic) }.fold(onSuccess = { inviteeAccount ->
            if (invitesRepository.checkIfAccountsHaveExistingInvite(inviterAccountId.value, inviteeAccount.accountId.value)) {
                return logger.error(AccountsAlreadyHaveInviteException)
            }

            if (threadsRepository.checkIfSelfAccountHaveThreadWithPeerAccount(inviteeAccount.accountId.value, inviterAccountId.value)) {
                return logger.error(AccountsAlreadyHaveThreadException)
            }

            val inviteePublicKey = inviteeAccount.publicInviteKey ?: throw Throwable("Missing publicInviteKey")
            val inviterPublicKey = decodeX25519DidKey(claims.inviterPublicKey)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
            val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            val invite = Invite.Received(
                wcRequest.id, inviterAccountId, inviteeAccount.accountId, InviteMessage(claims.subject), inviterPublicKey, InviteStatus.PENDING, acceptTopic, symmetricKey, inviterPrivateKey = null,
                //todo: use publishedAt from relay https://github.com/WalletConnect/WalletConnectKotlinV2/issues/872
                timestamp = wcRequest.id.extractTimestamp()
            )

            invitesRepository.insertInvite(invite)
            _events.emit(Events.OnInvite(invite))
        }, onFailure = { error -> logger.error(error) })
    }
}