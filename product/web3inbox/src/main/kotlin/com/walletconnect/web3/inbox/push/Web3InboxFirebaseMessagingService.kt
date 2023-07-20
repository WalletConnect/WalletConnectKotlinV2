package com.walletconnect.web3.inbox.push

import android.annotation.SuppressLint
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushMessageService


//todo this will need to be implemented by sdk consumers, add to docs
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class Web3InboxFirebaseMessagingService : PushMessageService() {
    sealed interface Web3InboxMessage {
        data class ChatMessage(val message: String) : Web3InboxMessage
//        data class PushMessage(val title: String, val body: String, val icon: String?, val url: String?) : Web3InboxMessage
    }

    abstract fun onMessage(message: Web3InboxMessage, originalMessage: RemoteMessage)

    override fun onDefaultBehavior(message: RemoteMessage) {
        onMessage(Web3InboxMessage.ChatMessage("You got a new message!!!"), message)
    }


    override fun onMessage(message: Push.Model.Message, originalMessage: RemoteMessage) {
        // hide
    }
}