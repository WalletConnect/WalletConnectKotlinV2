@file:JvmSynthetic

package com.walletconnect.chat.common.model

internal sealed class Events {
    data class OnInvite(val id: Int, val invite: Invite) : Events()

    data class OnJoined(val topic: String) : Events()

    data class OnMessage(val topic: String, val message: Message) : Events()

    data class OnLeft(val topic: String) : Events()
}