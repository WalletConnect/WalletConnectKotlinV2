@file:JvmSynthetic

package com.walletconnect.chat.engine.model



internal sealed class EngineDO {
    data class Error(val throwable: Throwable) : EngineDO() // TODO: Should this be extracted to core for easier error handling?

    data class Invite(
        val account: String,
        val message: String,
        val signature: String? = null
    ) : EngineDO()

    data class Media(
        val type: String,
        val data: String,
    ) : EngineDO()

    data class Thread(
        val topic: String,
        val selfAccount: String,
        val peerAccount: String,
    ) : EngineDO()

    data class Message(
        val message: String,
        val authorAccount: String,
        val timestamp: Long,
        val media: Media
    ) : EngineDO()

    data class Account(
        val account: String,
        val publicKey: String,
    ) : EngineDO()

    sealed class Events : EngineDO() {
        data class OnInvite(val id: Int, val invite: Invite) : Events()

        data class OnJoined(val topic: String) : Events()

        data class OnMessage(val topic: String, val message: Message) : Events()

        data class OnLeft(val topic: String) : Events()
    }
}