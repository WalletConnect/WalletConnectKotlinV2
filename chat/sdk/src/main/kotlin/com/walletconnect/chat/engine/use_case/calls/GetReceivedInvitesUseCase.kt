package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.storage.InvitesStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking


internal class GetReceivedInvitesUseCase(
    private val invitesRepository: InvitesStorageRepository,
) : GetReceivedInvitesUseCaseInterface {

    override fun getReceivedInvites(inviteeAccountId: String): Map<Long, Invite.Received> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return runBlocking(scope.coroutineContext) {
            invitesRepository.getReceivedInvitesForInviteeAccount(inviteeAccountId).associateBy { invite -> invite.id }
        }
    }
}

internal interface GetReceivedInvitesUseCaseInterface {
    fun getReceivedInvites(inviteeAccountId: String): Map<Long, Invite.Received>
}