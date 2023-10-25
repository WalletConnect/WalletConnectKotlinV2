package com.walletconnect.sample.wallet.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.ui.Web3WalletActivity
import timber.log.Timber
import java.net.URI

object NotificationHandler {

    fun showNotification(message: Notify.Model.Message, context: Context) {
        val intent by lazy { Intent(context, Web3WalletActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) } }
        val notificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
        val pendingIntent by lazy { PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE) }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val (channelId, channelName) = when (message) {
            is Notify.Model.Message.Simple -> "Web3Wallet" to "Web3Wallet"
            is Notify.Model.Message.Decrypted -> message.type to runCatching {
                // TODO discus with the team how to make it more dev friendly
                val appMetadata = NotifyClient.getActiveSubscriptions()[message.topic]?.metadata ?: throw IllegalStateException("No active subscription for topic: ${message.topic}")
                val appDomain = URI(appMetadata.url).host ?: throw IllegalStateException("Unable to parse domain from $appMetadata.url")

                val typeName = NotifyClient.getNotificationTypes(Notify.Params.NotificationTypes(appDomain))[message.type]?.name
                    ?: throw IllegalStateException("No notification type for topic:${message.topic} and type: ${message.type}")
                (appMetadata.name + ": " + typeName)
            }.getOrElse {
                Timber.e(it)
                message.type
            }

        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentText(message.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(message.hashCode(), notificationBuilder.build())
    }
}