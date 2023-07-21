package com.walletconnect.push.client

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.CoreClient

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class PushMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoreClient.Echo.register(token,
            onSuccess = {
                newToken(token)
            },
            onError = { error ->
                registeringFailed(token, error)
            }
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        with(message) {
            try {
                if (data.containsKey("topic") && data.containsKey("blob")) {
                    val encryptedMessage = Push.Params.DecryptMessage(topic = data.getValue("topic"), encryptedMessage = data.getValue("blob"))

                    PushWalletClient.decryptMessage(encryptedMessage,
                        onSuccess = { pushMessage -> onMessage(pushMessage, this) },
                        onError = { error -> onError(error.throwable, this) }
                    )
                } else if (notification?.isValid() == true) {
                    val pushMessage = with(requireNotNull(notification)) {
                        Push.Model.Message(
                            title = title!!,
                            body = body!!,
                            icon = icon,
                            url = imageUrl?.toString(),
                            type = ""
                        )
                    }

                    onMessage(pushMessage, this)
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

    abstract fun onMessage(message: Push.Model.Message, originalMessage: RemoteMessage)

    abstract fun onDefaultBehavior(message: RemoteMessage)

    abstract fun onError(throwable: Throwable, defaultMessage: RemoteMessage)

    private fun RemoteMessage.Notification?.isValid(): Boolean = this != null && title != null && body != null
}