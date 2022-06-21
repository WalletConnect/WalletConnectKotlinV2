package com.walletconnect.chatsample.ui.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.chat.client.Chat
import com.walletconnect.chatsample.domain.ChatDelegate
import com.walletconnect.chatsample.tag
import com.walletconnect.chatsample.ui.ChatSampleEvents
import com.walletconnect.chatsample.ui.toChatSampleEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class ChatSampleViewModel : ViewModel() {
    val emittedEvents: Flow<ChatSampleEvents> = ChatDelegate.wcEventModels.map { walletEvent: Chat.Model.Events ->
        Log.d(tag(this), walletEvent.toString())
        when (walletEvent) {
            is Chat.Model.Events.OnInvite -> walletEvent.toChatSampleEvent()
            is Chat.Model.Events.OnJoined -> walletEvent.toChatSampleEvent()
            is Chat.Model.Events.OnLeft -> walletEvent.toChatSampleEvent()
            is Chat.Model.Events.OnMessage -> walletEvent.toChatSampleEvent()
            else -> ChatSampleEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

}