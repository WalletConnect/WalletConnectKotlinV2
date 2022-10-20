package com.walletconnect.chatsample

import android.app.Application
import android.util.Log
import com.walletconnect.android.RelayClient
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chatsample.utils.tag

const val WALLET_CONNECT_PROD_RELAY_URL = "relay.walletconnect.com"

class ChatSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        RelayClient.initialize(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this)

        ChatClient.initialize(Chat.Params.Init(RelayClient, "https://keys.walletconnect.com")) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}