package com.walletconnect.chat.storage

import com.walletconnect.chat.common.model.*
import com.walletconnect.chat.storage.data.dao.InvitesQueries
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal class InvitesStorageRepository(private val invites: InvitesQueries) {
    suspend fun insertInvite(invite: Invite) {
        val type = when (invite) {
            is Invite.Received -> InviteType.RECEIVED
            is Invite.Sent -> InviteType.SENT
        }

        with(invite) {
            invites.insertOrAbortInvite(
                inviteId = id, message = message.value, inviterAccount = inviterAccount.value,
                inviteeAccount = inviteeAccount.value, status = InviteStatus.PENDING, type = type, acceptTopic = acceptTopic.value,
                inviterPublicKey = inviterPublicKey.keyAsHex, inviteePublicKey = inviteePublicKey.keyAsHex
            )
        }
    }

    fun checkIfAccountsHaveExistingInvite(inviterAccount: String, inviteeAccount: String): Boolean = invites.checkIfAccountsHaveExistingInvite(inviterAccount, inviteeAccount).executeAsOne()

    suspend fun getAllPendingSentInvites() = invites.getAllPendingSentInvites(::dbToSentInvite).executeAsList()

    suspend fun deleteInviteByInviteId(inviteId: Long) = invites.deleteInviteByInviteId(inviteId)

    suspend fun getReceivedInviteByInviteId(inviteId: Long) = invites.getInviteByInviteId(inviteId, ::dbToReceivedInvite).executeAsOne()

    suspend fun getSentInviteByInviteId(inviteId: Long) = invites.getInviteByInviteId(inviteId, ::dbToSentInvite).executeAsOne()

    suspend fun updateStatusByInviteId(inviteId: Long, status: InviteStatus) = invites.updateInviteStatus(status, inviteId)

    suspend fun getSentInvitesForInviterAccount(inviterAccount: String): List<Invite.Sent> = invites.getSentInvitesForInviterAccount(inviterAccount, ::dbToSentInvite).executeAsList()

    suspend fun getReceivedInvitesForInviteeAccount(inviterAccount: String): List<Invite.Received> = invites.getReceivedInvitesForInviteeAccount(inviterAccount, ::dbToReceivedInvite).executeAsList()

    private fun dbToSentInvite(
        inviteId: Long, message: String, inviterAccount: String, inviteeAccount: String,
        status: InviteStatus, inviterPublicKey: String, inviteePublicKey: String, acceptTopic: String,
    ): Invite.Sent = Invite.Sent(
        id = inviteId, inviterAccount = AccountId(inviterAccount), inviteeAccount = AccountId(inviteeAccount),
        message = InviteMessage(message), inviterPublicKey = PublicKey(inviterPublicKey),
        inviteePublicKey = PublicKey(inviteePublicKey), status = status, acceptTopic = Topic(acceptTopic)
    )

    private fun dbToReceivedInvite(
        inviteId: Long, message: String, inviterAccount: String, inviteeAccount: String,
        status: InviteStatus, inviterPublicKey: String, inviteePublicKey: String, acceptTopic: String,
    ): Invite.Received = Invite.Received(
        id = inviteId, inviterAccount = AccountId(inviterAccount), inviteeAccount = AccountId(inviteeAccount),
        message = InviteMessage(message), inviterPublicKey = PublicKey(inviterPublicKey),
        inviteePublicKey = PublicKey(inviteePublicKey), status = status, acceptTopic = Topic(acceptTopic)
    )
}
