package com.walletconnect.chatsample.ui.shared

import com.walletconnect.chat.client.Chat

sealed class ChatSharedEvents {
    object NoAction : ChatSharedEvents()

    data class OnInvite(val id: Long, val invite: Chat.Model.Invite) : ChatSharedEvents()
    data class OnJoined(val topic: String) : ChatSharedEvents()
    data class OnMessage(val topic: String, val message: Chat.Model.Message) : ChatSharedEvents()
    data class OnLeft(val topic: String) : ChatSharedEvents()
}

fun Chat.Model.Events.OnInvite.toChatSharedEvents() = ChatSharedEvents.OnInvite(id, invite)
fun Chat.Model.Events.OnJoined.toChatSharedEvents() = ChatSharedEvents.OnJoined(topic)
fun Chat.Model.Events.OnMessage.toChatSharedEvents() = ChatSharedEvents.OnMessage(topic, message)
fun Chat.Model.Events.OnLeft.toChatSharedEvents() = ChatSharedEvents.OnLeft(topic)