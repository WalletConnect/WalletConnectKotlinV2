package com.example.pushapp

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.sign.client.SignClient

class PushFCM : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.e("kobe", "RemoteMessage: ${message.data["title"]}")

        Log.e("kobe", "Insering CALLBACK")

        val topic = SignClient.insert()
        Log.e("kobe", "Topic: $topic")

        val exo = SignClient.get()
        Log.e("kobe", "Expiry: $exo")
    }
}