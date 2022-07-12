package com.walletconnect.chatsample.ui

import com.walletconnect.chat.client.Chat

sealed class ChatSampleEvents {
    object NoAction : ChatSampleEvents()

    data class OnInvite(val id: Long, val invite: Chat.Model.Invite) : ChatSampleEvents()
    data class OnJoined(val topic: String) : ChatSampleEvents()
    data class OnMessage(val topic: String, val message: Chat.Model.Message) : ChatSampleEvents()
    data class OnLeft(val topic: String) : ChatSampleEvents()
}

fun Chat.Model.Events.OnInvite.toChatSampleEvent() = ChatSampleEvents.OnInvite(id, invite)
fun Chat.Model.Events.OnJoined.toChatSampleEvent() = ChatSampleEvents.OnJoined(topic)
fun Chat.Model.Events.OnMessage.toChatSampleEvent() = ChatSampleEvents.OnMessage(topic, message)
fun Chat.Model.Events.OnLeft.toChatSampleEvent() = ChatSampleEvents.OnLeft(topic)