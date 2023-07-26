package com.walletconnect.sample.web3inbox

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.sample.web3inbox.ui.routes.W3ISampleActivity
import com.walletconnect.web3.inbox.client.Inbox
import com.walletconnect.web3.inbox.push.Web3InboxFirebaseMessagingService
import timber.log.Timber
import kotlin.random.Random
import kotlin.random.nextUInt

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class W3IFirebaseMessagingService : Web3InboxFirebaseMessagingService() {
    private val intent by lazy { Intent(this, W3ISampleActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) } }
    private val pendingIntent by lazy { PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE) }
    private val channelId = "Push"
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }


    override fun onMessage(message: Inbox.Model.Message, originalMessage: RemoteMessage) {
        Timber.d("W3IFirebaseMessagingService onW3IMessage:\t$message")

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setContentText(message.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(Random.nextUInt().toInt(), notificationBuilder.build())
    }


    override fun newToken(token: String) {
        Timber.d("W3IFirebaseMessagingService newToken: $token")
    }

    override fun onDefaultBehavior(message: RemoteMessage) {
        Timber.d("W3IFirebaseMessagingService onDefaultBehavior: $message.toString()")
    }


    override fun onError(throwable: Throwable, defaultMessage: RemoteMessage) {
        Timber.e("W3IFirebaseMessagingService onError: $throwable")
    }


    override fun registeringFailed(token: String, throwable: Throwable) {
        Timber.e("W3IFirebaseMessagingService registeringFailed: $throwable")
    }
}