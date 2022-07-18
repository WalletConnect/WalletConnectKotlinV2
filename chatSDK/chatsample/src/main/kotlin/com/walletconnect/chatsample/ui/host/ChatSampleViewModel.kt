package com.walletconnect.chatsample.ui.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
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

    fun resolve(listener: Chat.Listeners.Resolve) {
        ChatClient.resolve(Chat.Params.Resolve(Chat.Model.AccountId(PEER_ACCOUNT)), listener)
    }

    fun register(listener: Chat.Listeners.Register) {
            ChatClient.register(Chat.Params.Register(Chat.Model.AccountId(SELF_ACCOUNT)), listener)
    }

    fun invite(publicKey: String) {
        Log.d(tag(this), "Invite PubKey X: $publicKey")

        ChatClient.addContact(
            Chat.Params.AddContact(Chat.Model.AccountId(PEER_ACCOUNT), publicKey)
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }

        ChatClient.invite(
            Chat.Params.Invite(
                Chat.Model.AccountId(PEER_ACCOUNT),
                Chat.Model.Invite(Chat.Model.AccountId(SELF_ACCOUNT), "Let me in!!!")
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }

    fun accept(id: Long) {
        Log.d(tag(this), "Accept id: $id")

        ChatClient.accept(Chat.Params.Accept(id)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }

    fun message(topic: String, message: String) {
        Log.d(tag(this), "Message topic: $topic, message: $message")

        ChatClient.message(Chat.Params.Message(topic, message)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }

    companion object {
        const val SELF_ACCOUNT = "eip:1:0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
//        const val PEER_ACCOUNT = "eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb" // for cross-platform testing
        const val PEER_ACCOUNT = SELF_ACCOUNT // for kotlin testing
    }
}