package com.walletconnect.web3.wallet

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.push.common.Push
import com.walletconnect.push.wallet.client.PushMessageService
import com.walletconnect.web3.wallet.ui.Web3WalletActivity
import kotlin.random.Random
import kotlin.random.nextUInt

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class WalletFirebaseMessagingService: PushMessageService() {
    private val TAG = this::class.simpleName
    private val intent by lazy { Intent(this, Web3WalletActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) } }
    private val pendingIntent by lazy { PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE) }
    private val channelId = "Push"
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun newToken(token: String) {
        Log.d(TAG, "Registering New Token Success:\t$token")
    }

    override fun registeringFailed(token: String, throwable: Throwable) {
        Log.d(TAG, "Registering New Token Failed:\t$token")
    }

    override fun onMessage(message: Push.Model.Message, originalMessage: RemoteMessage) {
        Log.d(TAG, "Message:\t$message")

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setSmallIcon(R.drawable.wc_icon_round)
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

    override fun onDefaultBehavior(message: RemoteMessage) {
        Log.d(TAG, "onDefaultBehavior: ${message.to}")
    }

    override fun onError(throwable: Throwable, defaultMessage: RemoteMessage) {
        Log.e(TAG, "onError Message To: ${defaultMessage.to}", throwable)
    }
}