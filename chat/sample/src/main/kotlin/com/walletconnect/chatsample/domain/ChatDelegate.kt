package com.walletconnect.chatsample.domain

import android.util.Log
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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

    override fun onError(error: Chat.Model.Error) {
        Log.e("ChatDelegate", "Internal error: $error")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}