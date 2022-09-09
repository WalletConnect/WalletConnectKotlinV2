@file:JvmSynthetic

package com.walletconnect.chat.engine.model

import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.core.model.vo.MediaVO

internal sealed class EngineDO {
    data class Error(val throwable: Throwable) : EngineDO() // TODO: Should this be extracted to core for easier error handling?

    data class Invite(
        val accountId: AccountIdVO,
        val message: String,
        val signature: String? = null,
    ) : EngineDO()

    data class Thread(
        val topic: String,
        val selfAccountId: AccountIdVO,
        val peerAccountId: AccountIdVO,
    ) : EngineDO()

    data class Message(
        val message: String,
        val authorAccountId: AccountIdVO,
        val timestamp: Long,
        val media: MediaVO?,
    ) : EngineDO()

    data class Contact(
        val accountIdWithPublicKeyVO: AccountIdWithPublicKeyVO,
        val displayName: String,
    ) : EngineDO()

    data class SendMessage(
        val author: AccountIdVO,
        val message: String,
        val media: MediaVO?,
    ) : EngineDO()

    sealed class Events : EngineDO() {
        data class OnInvite(val id: Long, val invite: Invite) : Events()

        data class OnJoined(val topic: String) : Events()

        data class OnMessage(val topic: String, val message: Message) : Events()

        data class OnLeft(val topic: String) : Events()
    }
}