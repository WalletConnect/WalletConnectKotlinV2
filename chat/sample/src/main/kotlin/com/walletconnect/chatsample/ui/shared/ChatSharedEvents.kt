package com.walletconnect.chatsample.ui.shared

import com.walletconnect.chat.client.Chat

sealed class ChatSharedEvents {
    object NoAction : ChatSharedEvents()

    data class OnInvite(val invite: Chat.Model.Invite.Received) : ChatSharedEvents()
    data class OnInviteAccepted(val topic: String) : ChatSharedEvents()
    data class OnInviteRejected(val topic: String) : ChatSharedEvents()
    data class OnMessage(val message: Chat.Model.Message) : ChatSharedEvents()
    data class OnLeft(val topic: String) : ChatSharedEvents()
}

fun Chat.Model.Events.OnInvite.toChatSharedEvents() = ChatSharedEvents.OnInvite(invite)
fun Chat.Model.Events.OnInviteAccepted.toChatSharedEvents() = ChatSharedEvents.OnInviteAccepted(topic)
fun Chat.Model.Events.OnMessage.toChatSharedEvents() = ChatSharedEvents.OnMessage(message)
fun Chat.Model.Events.OnLeft.toChatSharedEvents() = ChatSharedEvents.OnLeft(topic)
fun Chat.Model.Events.OnInviteRejected.toChatSharedEvents() = ChatSharedEvents.OnInviteRejected(topic)