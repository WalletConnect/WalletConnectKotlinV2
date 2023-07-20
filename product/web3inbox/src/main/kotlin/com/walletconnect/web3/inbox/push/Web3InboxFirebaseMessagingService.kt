package com.walletconnect.web3.inbox.push

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.CoreClient
import com.walletconnect.push.client.Push
import com.walletconnect.push.client.PushWalletClient
import com.walletconnect.web3.inbox.client.Inbox
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
abstract class Web3InboxFirebaseMessagingService : FirebaseMessagingService() {

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
                if (data.containsKey("topic") && data.containsKey("blob") && data.containsKey("flags")) {
                    when (MessageFlags.findMessageFlag(data.getValue("flags"))) {
                        MessageFlags.ENCRYPTED -> {
                            val encryptedMessage = Push.Params.DecryptMessage(topic = data.getValue("topic"), encryptedMessage = data.getValue("blob"))

                            PushWalletClient.decryptMessage(encryptedMessage,
                                onSuccess = { pushMessage ->
                                    val inboxMessage = Inbox.Model.Message.Decrypted(
                                        title = pushMessage.title,
                                        body = pushMessage.body,
                                        icon = pushMessage.icon,
                                        url = pushMessage.url,
                                        type = pushMessage.type
                                    )

                                    onMessage(inboxMessage, this)
                                },
                                onError = { error ->
                                    onError(error.throwable, this)
                                }
                            )
                        }

                        MessageFlags.CHAT, MessageFlags.PUSH, MessageFlags.SIGN, MessageFlags.AUTH -> {
                            try {
                                val decodedMessage = Base64.decode(data.getValue("blob")).decodeToString()
                                val inboxSimpleMessage = JSONObject(decodedMessage)
                                    .takeIf { it.has("title") && it.has("body") }
                                    ?.run {
                                        Inbox.Model.Message.Simple(
                                            title = getString("title"),
                                            body = getString("body"),
                                        )
                                    } ?: throw IllegalArgumentException("Invalid message format")

                                onMessage(inboxSimpleMessage, this)
                            } catch (e: Exception) {
                                onError(e, this)
                            }
                        }
                    }
                } else if (notification?.isValid() == true) {
                    val simpleMessage = with(requireNotNull(notification)) {
                        Inbox.Model.Message.Simple(
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

    abstract fun onMessage(message: Inbox.Model.Message, originalMessage: RemoteMessage)

    abstract fun onDefaultBehavior(message: RemoteMessage)

    abstract fun onError(throwable: Throwable, defaultMessage: RemoteMessage)

    private fun RemoteMessage.Notification?.isValid(): Boolean = this != null && title != null && body != null

    // https://github.com/WalletConnect/walletconnect-docs/blob/a867df86ac1a6153a94cdda4b6e0dbd49f4dbb5a/docs/specs/servers/echo/spec.md?plain=1#L98
    private enum class MessageFlags(val value: Int) {
        SIGN(1 shl 1),
        AUTH(1 shl 2),
        CHAT(1 shl 3),
        PUSH(1 shl 4),
        ENCRYPTED((PUSH.value) + (1 shl 0));

        // function that takes a string, converts it to an int, and returns the enum value
        companion object {
            fun findMessageFlag(value: String): MessageFlags {
                return MessageFlags.values().find { it.value == value.toInt() } ?: throw IllegalArgumentException("Invalid value for MessageFlags")
            }
        }
    }
}