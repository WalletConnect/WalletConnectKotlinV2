package com.walletconnect.notify.client

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.echo.PushMessagingService
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class NotifyMessageService : PushMessagingService() {
    override fun onMessage(message: Core.Model.Message, originalMessage: RemoteMessage) {

        println("kobe: Notify Message: $message")

        when (message) {
            is Core.Model.Message.Notify -> onMessage(message.toNotify(), originalMessage)
            is Core.Model.Message.Simple -> onMessage(message.toNotify(), originalMessage)
            is Core.Model.Message.Decrypted -> TODO()
        }
    }

    abstract fun onMessage(message: Notify.Model.Message, originalMessage: RemoteMessage)

    private fun Core.Model.Message.Notify.toNotify(): Notify.Model.Message.Decrypted = Notify.Model.Message.Decrypted(title, body, icon, url, type, topic)
    private fun Core.Model.Message.Simple.toNotify(): Notify.Model.Message.Simple = Notify.Model.Message.Simple(title, body)
}