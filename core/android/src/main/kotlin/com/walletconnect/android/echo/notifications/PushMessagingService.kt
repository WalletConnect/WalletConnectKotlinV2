package com.walletconnect.android.echo.notifications //todo: should change package from echo to push?

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject
import org.koin.core.qualifier.named

abstract class PushMessagingService : FirebaseMessagingService() {
    private val decryptNotifyMessageUseCase: DecryptMessageUseCaseInterface by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.DECRYPT_NOTIFY_MESSAGE)) }
    private val decryptSignMessageUseCase: DecryptMessageUseCaseInterface by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.DECRYPT_SIGN_MESSAGE)) }
    private val decryptAuthMessageUseCase: DecryptMessageUseCaseInterface by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.DECRYPT_AUTH_MESSAGE)) }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoreClient.Echo.register(firebaseAccessToken = token, enableEncrypted = true, onSuccess = { newToken(token) }, onError = { error -> registeringFailed(token, error) })
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        println("kobe; Remote Message: ${message.data}")

        with(message) {
            try {
                when {
                    isLegacyNotification() -> {
                        when (MessageFlags.findMessageFlag(data.getValue(KEY_FLAGS))) {
                            MessageFlags.ENCRYPTED -> decryptNotifyMessage(data.getValue(KEY_BLOB))
                            MessageFlags.CHAT, MessageFlags.NOTIFY, MessageFlags.SIGN, MessageFlags.AUTH -> prepareSimpleNotification()
                        }
                    }

                    isEncryptedNotification() -> {
                        when (data.getValue(KEY_TAG)) {
                            Tags.SESSION_PROPOSE.id.toString(), Tags.SESSION_REQUEST.id.toString() -> decryptSignMessage()
                            Tags.AUTH_REQUEST.id.toString() -> decryptAuthMessage()
                            Tags.NOTIFY_MESSAGE.id.toString() -> decryptNotifyMessage(data.getValue(KEY_MESSAGE))
                        }
                    }

                    notification?.isValid() == true -> {
                        val simpleMessage = with(requireNotNull(notification)) { Core.Model.Message.Simple(title = title!!, body = body!!) }
                        onMessage(simpleMessage, this)
                    }

                    else -> onDefaultBehavior(this)
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
    private fun RemoteMessage.isEncryptedNotification() = data.containsKey(KEY_TOPIC) && data.containsKey(KEY_TAG) && data.containsKey(KEY_MESSAGE)

    private fun RemoteMessage.decryptNotifyMessage(encryptedMessage: String) {
        scope.launch {
            supervisorScope {
                decryptNotifyMessageUseCase.decryptMessage(data.getValue(KEY_TOPIC), encryptedMessage,
                    onSuccess = { message ->
                        if (message is Core.Model.Message.Notify) onMessage(message, this@decryptNotifyMessage)
                    },
                    onFailure = { throwable -> onError(throwable, this@decryptNotifyMessage) }
                )
            }
        }
    }

    private fun RemoteMessage.decryptSignMessage() {
        scope.launch {
            supervisorScope {
                decryptSignMessageUseCase.decryptMessage(data.getValue(KEY_TOPIC), data.getValue(KEY_MESSAGE),
                    onSuccess = { message ->
                        when (message) {
                            is Core.Model.Message.SessionProposal -> onMessage(message, this@decryptSignMessage)
                            is Core.Model.Message.SessionRequest -> onMessage(message, this@decryptSignMessage)
                            else -> onDefaultBehavior(this@decryptSignMessage)
                        }
                    },
                    onFailure = { throwable -> onError(throwable, this@decryptSignMessage) }
                )
            }
        }
    }

    private fun RemoteMessage.decryptAuthMessage() {
        println("kobe: onDecryptedAuthMessage")

        scope.launch {
            supervisorScope {
                decryptAuthMessageUseCase.decryptMessage(data.getValue(KEY_TOPIC), data.getValue(KEY_MESSAGE),
                    onSuccess = { message ->
                        println("kobe: Result auth: $message")
                        if (message is Core.Model.Message.AuthRequest) onMessage(message, this@decryptAuthMessage)

                    },
                    onFailure = { throwable -> onError(throwable, this@decryptAuthMessage) }
                )
            }
        }
    }

    private fun RemoteMessage.prepareSimpleNotification() {
        try {
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
        const val KEY_TOPIC = "topic"

        // For Encrypted Messages
        const val KEY_TAG = "tag"
        const val KEY_MESSAGE = "message"

        // For Legacy Messages
        const val KEY_BLOB = "blob"
        const val KEY_FLAGS = "flags"

        // For Simple Messages
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}