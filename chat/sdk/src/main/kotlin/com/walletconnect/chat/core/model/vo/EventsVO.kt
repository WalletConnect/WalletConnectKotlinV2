@file:JvmSynthetic

package com.walletconnect.chat.core.model.vo

internal sealed class EventsVO {
    data class OnInvite(val id: Int, val invite: InviteVO) : EventsVO()

    data class OnJoined(val topic: String) : EventsVO()

    data class OnMessage(val topic: String, val message: MessageVO) : EventsVO()

    data class OnLeft(val topic: String) : EventsVO()
}