package com.walletconnect.notify.client

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.CoreClient
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

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
                if (data.containsKey(KEY_TOPIC) && data.containsKey(KEY_BLOB) && data.containsKey(KEY_FLAGS)) {
                    when (MessageFlags.findMessageFlag(data.getValue(KEY_FLAGS))) {
                        MessageFlags.ENCRYPTED -> {
                            val encryptedMessage = Notify.Params.DecryptMessage(topic = data.getValue(KEY_TOPIC), encryptedMessage = data.getValue(KEY_BLOB))

                            NotifyClient.decryptMessage(encryptedMessage,
                                onSuccess = { notifyMessage ->
                                    onMessage(notifyMessage, this)
                                },
                                onError = { error ->
                                    onError(error.throwable, this)
                                }
                            )
                        }

                        MessageFlags.CHAT, MessageFlags.NOTIFY, MessageFlags.SIGN, MessageFlags.AUTH -> {
                            try {
                                val decodedMessage = Base64.decode(data.getValue(KEY_BLOB)).decodeToString()
                                val notifySimpleMessage = JSONObject(decodedMessage)
                                    .takeIf { it.has(KEY_TITLE) && it.has(KEY_BODY) }
                                    ?.run {
                                        Notify.Model.Message.Simple(
                                            title = getString(KEY_TITLE),
                                            body = getString(KEY_BODY),
                                        )
                                    } ?: throw IllegalArgumentException("Invalid message format")

                                onMessage(notifySimpleMessage, this)
                            } catch (e: Exception) {
                                onError(e, this)
                            }
                        }
                    }
                } else if (notification?.isValid() == true) {
                    val simpleMessage = with(requireNotNull(notification)) {
                        Notify.Model.Message.Simple(
                            title = title!!,
                            body = body!!,
                        )
                    }

                    onMessage(simpleMessage, this)
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

    // https://github.com/WalletConnect/walletconnect-docs/blob/a867df86ac1a6153a94cdda4b6e0dbd49f4dbb5a/docs/specs/servers/echo/spec.md?plain=1#L98
    private enum class MessageFlags(val value: Int) {
        SIGN(1 shl 1),
        AUTH(1 shl 2),
        CHAT(1 shl 3),
        NOTIFY(1 shl 4),
        ENCRYPTED((NOTIFY.value) + (1 shl 0));

        // function that takes a string, converts it to an int, and returns the enum value
        companion object {
            fun findMessageFlag(value: String): MessageFlags {
                return MessageFlags.values().find { it.value == value.toInt() } ?: throw IllegalArgumentException("Invalid value for MessageFlags")
            }
        }
    }

    private companion object {
        // For Decrypted Messages
        const val KEY_TOPIC = "topic"
        const val KEY_BLOB = "blob"
        const val KEY_FLAGS = "flags"

        // For Simple Messages
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}