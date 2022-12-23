package com.walletconnect.push.wallet.client

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.push.common.Push

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class PushMessageService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val fcmToken = Push.Wallet.Params.FcmToken(token)
        PushWalletClient.registerFcmToken(fcmToken,
            onSuccess = {
                newToken(token)
            },
            onError = { error ->
                registeringFailed(token, error.throwable)
            }
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        with(message) {
            try {
                if (data.containsKey("topic") && data.containsKey("message")) {
                    val encryptedMessage = Push.Wallet.Params.DecryptMessage(topic = data.getValue("topic"), encryptedMessage = data.getValue("message"))

                    PushWalletClient.decryptMessage(encryptedMessage,
                        onSuccess = { decryptedMessage -> onDecryptedMessage(decryptedMessage, this) },
                        onError = { error -> onError(error.throwable, this) }
                    )
                } else if (notification?.isValid() == true) {
                    val pushMessage = with (requireNotNull(notification)) {
                        Push.Model.Message(title!!, body!!, icon, imageUrl?.toString())
                    }

                    onUnencryptedMessage(pushMessage, this)
                } else {
                    onDefaultBehavior(this)
                }
            } catch (e: Exception) {
                onError(e, message)
            }
        }
    }

    abstract fun newToken(token: String)

    abstract fun registeringFailed(token: String, throwable: Throwable)

    abstract fun onDecryptedMessage(decryptedMessage: String, originalMessage: RemoteMessage)

    abstract fun onUnencryptedMessage(message: Push.Model.Message, originalMessage: RemoteMessage)

    abstract fun onDefaultBehavior(message: RemoteMessage)

    abstract fun onError(throwable: Throwable, defaultMessage: RemoteMessage)

    private fun RemoteMessage.Notification?.isValid(): Boolean = this != null && title != null && body != null
}