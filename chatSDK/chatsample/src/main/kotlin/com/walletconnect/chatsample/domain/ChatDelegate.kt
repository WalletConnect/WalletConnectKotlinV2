package com.walletconnect.chatsample.domain

import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

object ChatDelegate : ChatClient.ChatDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Chat.Model.Events> = MutableSharedFlow(1)
    val wcEventModels: SharedFlow<Chat.Model.Events> = _wcEventModels

    init {
        ChatClient.setChatDelegate(this)
    }

    override fun onInvite(onInvite: Chat.Model.Events.OnInvite) {
        scope.launch {
            _wcEventModels.emit(onInvite)
            clearCache()
        }
    }

    override fun onJoined(onJoined: Chat.Model.Events.OnJoined) {
        scope.launch {
            _wcEventModels.emit(onJoined)
            clearCache()
        }
    }

    override fun onMessage(onMessage: Chat.Model.Events.OnMessage) {
        scope.launch {
            _wcEventModels.emit(onMessage)
            clearCache()
        }}

    override fun onLeft(onLeft: Chat.Model.Events.OnLeft) {
        TODO("Not yet implemented")
    }

    fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}