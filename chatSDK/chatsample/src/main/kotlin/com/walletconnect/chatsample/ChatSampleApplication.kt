package com.walletconnect.chatsample

import android.app.Application
import android.util.Log
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.sample_common.tag

class ChatSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ChatClient.initialize(
            Chat.Params.Init(
                this,
                "http://159.65.123.131:8080"
            )
        ) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}