package com.walletconnect.android.echo

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

abstract class PushMessagingService : FirebaseMessagingService() {

    private val decryptNotifyMessageUseCase: DecryptMessageUseCaseInterface by lazy { wcKoinApp.koin.get() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        println("kobe; New Token: $token")
        //todo: add enableAlwaysDecrypted flag
        //todo: should be called whenever tha flag is changed!

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

        println("kobe; Remote Message: ${message.data}")

        with(message) {
            try {
                if (isLegacyNotification()) {
                    when (MessageFlags.findMessageFlag(data.getValue(KEY_FLAGS))) {
                        MessageFlags.ENCRYPTED -> decryptNotifyMessage()
                        MessageFlags.CHAT, MessageFlags.NOTIFY, MessageFlags.SIGN, MessageFlags.AUTH -> prepareSimpleNotification()
                    }
                } else if (notification?.isValid() == true) { //todo: add check for encrypted notification
                    println("kobe: onValidNotification")
                    val simpleMessage = with(requireNotNull(notification)) {
                        Core.Model.Message.Simple(
                            title = title!!,
                            body = body!!,
                        )
                    }

                    onMessage(simpleMessage, this)
                } else {
                    //todo: Check Tags for Decrypting the message
                    println("kobe: onDefaultMessage")
                    onDefaultBehavior(this)
                }
            } catch (e: Exception) {
                onError(e, message)
            }
        }
    }

    abstract fun onMessage(message: Core.Model.Message, originalMessage: RemoteMessage)

    abstract fun newToken(token: String)

    abstract fun registeringFailed(token: String, throwable: Throwable)

    abstract fun onDefaultBehavior(message: RemoteMessage)

    abstract fun onError(throwable: Throwable, defaultMessage: RemoteMessage)

    private fun RemoteMessage.isLegacyNotification() = data.containsKey(KEY_TOPIC) && data.containsKey(KEY_BLOB) && data.containsKey(KEY_FLAGS)

    private fun RemoteMessage.decryptNotifyMessage() {
        println("kobe: onNotifyEncryptedMessage")
        scope.launch {
            decryptNotifyMessageUseCase.decryptMessage(data.getValue(KEY_TOPIC), data.getValue(KEY_BLOB),
                onSuccess = { notifyMessage -> (notifyMessage as Message.Notify).run { onMessage(this.toCore(data.getValue(KEY_TOPIC)), this@decryptNotifyMessage) } },
                onFailure = { throwable -> onError(throwable, this@decryptNotifyMessage) }
            )
        }
    }

    private fun RemoteMessage.prepareSimpleNotification() {
        try {
            println("kobe: onSimpleMessage")
            val decodedMessage = Base64.decode(data.getValue(KEY_BLOB)).decodeToString()
            val notifySimpleMessage = JSONObject(decodedMessage)
                .takeIf { it.has(KEY_TITLE) && it.has(KEY_BODY) }
                ?.run { Core.Model.Message.Simple(getString(KEY_TITLE), getString(KEY_BODY)) } ?: throw IllegalArgumentException("Invalid message format")
            onMessage(notifySimpleMessage, this)
        } catch (e: Exception) {
            onError(e, this)
        }
    }

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

        fun Message.Notify.toCore(topic: String): Core.Model.Message.Notify = Core.Model.Message.Notify(title, body, icon, url, type, topic)
    }
}