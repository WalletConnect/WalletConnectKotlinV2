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

    override fun onInviteAccepted(onInviteAccepted: Chat.Model.Events.OnInviteAccepted) {
        scope.launch {
            _wcEventModels.emit(onInviteAccepted)
            clearCache()
        }
    }

    override fun onInviteRejected(onInviteRejected: Chat.Model.Events.OnInviteRejected) {
        scope.launch {
            _wcEventModels.emit(onInviteRejected)
            clearCache()
        }
    }

    override fun onMessage(onMessage: Chat.Model.Events.OnMessage) {
        scope.launch {
            _wcEventModels.emit(onMessage)
            clearCache()
        }
    }

    override fun onLeft(onLeft: Chat.Model.Events.OnLeft) {
        //todo: implement me
        Log.e("ChatDelegate", "On thread left")
    }

    override fun onConnectionStateChange(state: Chat.Model.ConnectionState) {
        Log.e("ChatDelegate", "On connection changed:$state")
    }

    override fun onError(error: Chat.Model.Error) {
        Log.e("ChatDelegate", "Internal error: $error")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearCache() {
        _wcEventModels.resetReplayCache()
    }
}