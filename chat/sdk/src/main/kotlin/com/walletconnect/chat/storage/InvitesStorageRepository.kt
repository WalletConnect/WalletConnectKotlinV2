package com.walletconnect.chat.storage

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteMessage
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.common.model.InviteType
import com.walletconnect.chat.storage.data.dao.InvitesQueries
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.Empty

internal class InvitesStorageRepository(private val invites: InvitesQueries) {
    suspend fun insertInvite(invite: Invite) {
        val type = when (invite) {
            is Invite.Received -> InviteType.RECEIVED
            is Invite.Sent -> InviteType.SENT
        }

        if (type == InviteType.SENT && invite.inviterPrivateKey == null) throw Throwable("Invite.Sent requires invitePrivateKey to not be null")

        val inviterPrivateKey = if (type == InviteType.SENT) invite.inviterPrivateKey!!.keyAsHex else String.Empty

        with(invite) {
            invites.insertOrAbortInvite(
                inviteId = id, message = message.value, inviterAccount = inviterAccount.value,
                inviteeAccount = inviteeAccount.value, status = InviteStatus.PENDING, type = type, acceptTopic = acceptTopic.value,
                inviterPublicKey = inviterPublicKey.keyAsHex, symmetricKey = symmetricKey.keyAsHex,
                inviterPrivateKey = inviterPrivateKey, timestamp = timestamp
            )
        }
    }

    suspend fun checkIfAccountsHaveExistingInvite(inviterAccount: String, inviteeAccount: String): Boolean = invites.checkIfAccountsHaveExistingInvite(inviterAccount, inviteeAccount).executeAsOne()

    suspend fun getAllPendingSentInvites() = invites.getAllPendingSentInvites(::dbToSentInvite).executeAsList()

    suspend fun deleteInviteByInviteId(inviteId: Long) = invites.deleteInviteByInviteId(inviteId)

    suspend fun getReceivedInviteByInviteId(inviteId: Long) = invites.getInviteByInviteId(inviteId, ::dbToReceivedInvite).executeAsOne()

    suspend fun getSentInviteByInviteId(inviteId: Long) = invites.getInviteByInviteId(inviteId, ::dbToSentInvite).executeAsOne()

    suspend fun updateStatusByInviteId(inviteId: Long, status: InviteStatus) = invites.updateInviteStatusByInviteId(status, inviteId)

    suspend fun updateStatusByAccounts(accountOne: String, accountTwo: String, status: InviteStatus) {
        // When it's impossible to distinct which account is the inviter try updating two
        // Very unlikely non-hostile drawback: When both accounts are register within Chat SDK and they invite each other first out of two updates will update two invites: SENT and RECEIVED
        invites.updateInviteStatusByAccounts(status, accountOne, accountTwo)
        invites.updateInviteStatusByAccounts(status, accountTwo, accountOne)
    }

    suspend fun getSentInvitesForInviterAccount(inviterAccount: String): List<Invite.Sent> = invites.getSentInvitesForInviterAccount(inviterAccount, ::dbToSentInvite).executeAsList()

    suspend fun getReceivedInvitesForInviteeAccount(inviterAccount: String): List<Invite.Received> = invites.getReceivedInvitesForInviteeAccount(inviterAccount, ::dbToReceivedInvite).executeAsList()

    private fun dbToSentInvite(
        inviteId: Long, message: String, inviterAccount: String, inviteeAccount: String,
        status: InviteStatus, inviterPublicKey: String, acceptTopic: String, symmetricKey: String, inviterPrivateKey: String, timestamp: Long,
    ): Invite.Sent = Invite.Sent(
        id = inviteId,
        inviterAccount = AccountId(inviterAccount),
        inviteeAccount = AccountId(inviteeAccount),
        message = InviteMessage(message),
        inviterPublicKey = PublicKey(inviterPublicKey),
        status = status,
        acceptTopic = Topic(acceptTopic),
        symmetricKey = SymmetricKey(symmetricKey),
        inviterPrivateKey = PrivateKey(inviterPrivateKey),
        timestamp = timestamp,
    )

    private fun dbToReceivedInvite(
        inviteId: Long, message: String, inviterAccount: String, inviteeAccount: String,
        status: InviteStatus, inviterPublicKey: String, acceptTopic: String, symmetricKey: String, inviterPrivateKey: String, // unused but required by select query
        timestamp: Long,
    ): Invite.Received = Invite.Received(
        id = inviteId,
        inviterAccount = AccountId(inviterAccount),
        inviteeAccount = AccountId(inviteeAccount),
        message = InviteMessage(message),
        inviterPublicKey = PublicKey(inviterPublicKey),
        status = status,
        acceptTopic = Topic(acceptTopic),
        symmetricKey = SymmetricKey(symmetricKey),
        inviterPrivateKey = null,
        timestamp = timestamp,
    )
}
