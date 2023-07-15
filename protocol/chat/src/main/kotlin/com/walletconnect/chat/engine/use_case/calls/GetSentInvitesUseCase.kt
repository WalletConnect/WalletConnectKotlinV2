package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.storage.InvitesStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking


internal class GetSentInvitesUseCase(
    private val invitesRepository: InvitesStorageRepository,
) : GetSentInvitesUseCaseInterface {

    override fun getSentInvites(inviterAccountId: String): Map<Long, Invite.Sent> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            invitesRepository.getSentInvitesForInviterAccount(inviterAccountId).associateBy { invite -> invite.id }
        }
    }
}

internal interface GetSentInvitesUseCaseInterface {
    fun getSentInvites(inviterAccountId: String): Map<Long, Invite.Sent>
}