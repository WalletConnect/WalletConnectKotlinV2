package com.walletconnect.notify.client

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.CoreClient
import com.walletconnect.util.Empty

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class NotifyMessageService : FirebaseMessagingService() {

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
                if (data.containsKey(KEY_TOPIC) && data.containsKey(KEY_BLOB)) {
                    val encryptedMessage = Notify.Params.DecryptMessage(topic = data.getValue(KEY_TOPIC), encryptedMessage = data.getValue(KEY_BLOB))

                    NotifyClient.decryptMessage(encryptedMessage,
                        onSuccess = { notifyMessage -> onMessage(notifyMessage, this) },
                        onError = { error -> onError(error.throwable, this) }
                    )
                } else if (notification?.isValid() == true) {
                    val notifyMessage = with(requireNotNull(notification)) {
                        Notify.Model.Message(
                            title = title!!,
                            body = body!!,
                            icon = icon,
                            url = imageUrl?.toString(),
                            type = String.Empty
                        )
                    }

                    onMessage(notifyMessage, this)
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

    abstract fun onMessage(message: Notify.Model.Message, originalMessage: RemoteMessage)

    abstract fun onDefaultBehavior(message: RemoteMessage)

    abstract fun onError(throwable: Throwable, defaultMessage: RemoteMessage)

    private fun RemoteMessage.Notification?.isValid(): Boolean = this != null && title != null && body != null

    private companion object {
        const val KEY_TOPIC = "topic"
        const val KEY_BLOB = "blob"
    }
}