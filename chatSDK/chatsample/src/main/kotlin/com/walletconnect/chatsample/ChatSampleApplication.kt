package com.walletconnect.chatsample

import android.app.Application
import android.util.Log
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chatsample.utils.tag

class ChatSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ChatClient.initialize(
            Chat.Params.Init(
                this,
                "https://keys.walletconnect.com"
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}