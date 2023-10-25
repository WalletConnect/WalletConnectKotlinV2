package com.walletconnect.sample.wallet

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyMessageService
import com.walletconnect.sample.wallet.domain.NotificationHandler

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class WalletFirebaseMessagingService : NotifyMessageService() {
    private val TAG = this::class.simpleName

    override fun newToken(token: String) {
        Log.d(TAG, "Registering New Token Success:\t$token")
    }

    override fun registeringFailed(token: String, throwable: Throwable) {
        Log.d(TAG, "Registering New Token Failed:\t$token")
    }

    override fun onMessage(message: Notify.Model.Message, originalMessage: RemoteMessage) {
        Log.d(TAG, "Message:\t$message")

        NotificationHandler.showNotification(message, this)
    }

    override fun onDefaultBehavior(message: RemoteMessage) {
        Log.d(TAG, "onDefaultBehavior: ${message.to}")
    }

    override fun onError(throwable: Throwable, defaultMessage: RemoteMessage) {
        Log.e(TAG, "onError Message To: ${defaultMessage.to}", throwable)
    }
}