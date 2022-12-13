package com.example.pushapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.sign.client.SignClient

class PushFCM : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.e("kobe", "RemoteMessage: ${message.data["title"]}")
        Log.e("kobe", "Insering CALLBACK")

        val topic = SignClient.insert() //insert record to DB
        Log.e("kobe", "Topic: $topic")

        val (expiry, key) = SignClient.get() //get key

        val decryptedMessage = SignClient.decryptMessage(topic, message.data["message"]!!) //decrypt encrypted message
        Log.e("kobe", "Expiry: $expiry; Decrypted message: $decryptedMessage")

        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, "jakob_channel_id")
            .setContentTitle(message.data["title"] ?: "Empty title")
            .setContentText("Expiry$expiry;Decrypted message: $decryptedMessage")
            .setSmallIcon(com.google.android.material.R.drawable.ic_clock_black_24dp)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }

        val exo = SignClient.get()
        Log.e("kobe", "Expiry: $exo")
    }

    private fun createNotificationChannel() {
        val name = "jakob_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("jakob_channel_id", name, importance)
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}