package com.walletconnect.web3.inbox.chat

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chat.client.ChatInterface
import com.walletconnect.foundation.util.Logger
import com.walletconnect.web3.inbox.chat.event.*
import com.walletconnect.web3.inbox.chat.event.OnInviteAcceptedChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnInviteChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnInviteRejectedChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnLeftChatEventUseCase
import com.walletconnect.web3.inbox.chat.event.OnMessageChatEventUseCase


internal class ChatEventHandler(
    private val logger: Logger,
    private val onInviteEventUseCase: OnInviteChatEventUseCase,
    private val onMessageEventUseCase: OnMessageChatEventUseCase,
    private val onInviteAcceptedEventUseCase: OnInviteAcceptedChatEventUseCase,
    private val onInviteRejectedEventUseCase: OnInviteRejectedChatEventUseCase,
    private val onLeftEventUseCase: OnLeftChatEventUseCase,
) : ChatClient.ChatDelegate {

    init {
        logger.log("ChatEventHandler init ")
    }

    override fun onInvite(onInvite: Chat.Model.Events.OnInvite) {
        logger.log("onInvite: $onInvite")
        onInviteEventUseCase(onInvite)
    }

    override fun onInviteAccepted(onInviteAccepted: Chat.Model.Events.OnInviteAccepted) {
        logger.log("onJoined: $onInviteAccepted")
        onInviteAcceptedEventUseCase(onInviteAccepted)
    }

    override fun onInviteRejected(onInviteRejected: Chat.Model.Events.OnInviteRejected) {
        logger.log("onReject: $onInviteRejected")
        onInviteRejectedEventUseCase(onInviteRejected)
    }

    override fun onMessage(onMessage: Chat.Model.Events.OnMessage) {
        logger.log("onMessage: $onMessage")
        onMessageEventUseCase(onMessage)
    }

    override fun onLeft(onLeft: Chat.Model.Events.OnLeft) {
        logger.log("onLeft: $onLeft")
        onLeftEventUseCase(onLeft)
    }

    override fun onConnectionStateChange(state: Chat.Model.ConnectionState) {
        logger.log("onConnectionStateChange: $state")
    }

    override fun onError(error: Chat.Model.Error) {
        logger.log("ChatEventHandler.onError: $error")
    }
}