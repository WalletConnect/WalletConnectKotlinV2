@file:JvmSynthetic

package com.walletconnect.chat.engine.model

import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.chat.common.model.Media

internal sealed class EngineDO {
    data class Error(val throwable: Throwable) : EngineDO()

    data class Invite(
        val accountId: AccountId,
        val message: String,
        val publicKey: String,
        val signature: String? = null,
    ) : EngineDO()

    data class Thread(
        val topic: String,
        val selfAccountId: AccountId,
        val peerAccountId: AccountId,
    ) : EngineDO()

    data class Message(
        val message: String,
        val authorAccountId: AccountId,
        val timestamp: Long,
        val media: Media?,
    ) : EngineDO()

    data class Contact(
        val accountIdWithPublicKey: AccountIdWithPublicKey,
        val displayName: String,
    ) : EngineDO()

    data class SendMessage(
        val author: AccountId,
        val message: String,
        val media: Media?,
    ) : EngineDO()

    sealed class Events : EngineDO(), EngineEvent {
        data class OnInvite(val id: Long, val invite: Invite) : Events()
        data class OnJoined(val topic: String) : Events()
        data class OnReject(val topic: String) : Events()
        data class OnMessage(val topic: String, val message: Message) : Events()
        data class OnLeft(val topic: String) : Events()
    }
}