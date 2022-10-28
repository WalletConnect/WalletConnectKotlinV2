package com.walletconnect.chatsample

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.chat.client.Chat
import com.walletconnect.chat.client.ChatClient
import com.walletconnect.chatsample.utils.tag

const val WALLET_CONNECT_PROD_RELAY_URL = "relay.walletconnect.com"

class ChatSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val metadata = Core.Model.AppMetaData(
            name = "Kotlin Wallet",
            description = "Wallet description",
            url = "example.wallet",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-wallet-wc:/request",
        )
        val serverUri = "wss://$WALLET_CONNECT_PROD_RELAY_URL?projectId=${BuildConfig.PROJECT_ID}"
        CoreClient.initialize(relayServerUrl = serverUri, connectionType = ConnectionType.AUTOMATIC, application = this, metaData = metadata)

        ChatClient.initialize(Chat.Params.Init(CoreClient)) { error ->
            Log.e(tag(this), error.throwable.stackTraceToString())
        }
    }
}