package com.walletconnect.notify.client

import android.net.Uri
import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.foundation.common.model.PrivateKey

object Notify {

    sealed class Model {

        @Deprecated("We renamed sealed class to Notification for consistency")
        sealed class Message : Model() {
            abstract val title: String
            abstract val body: String

            data class Simple(
                override val title: String,
                override val body: String,
            ) : Message()

            data class Decrypted(
                override val title: String,
                override val body: String,
                val icon: String?,
                val url: String?,
                val type: String,
                val topic: String,
            ) : Message()
        }

        sealed class Notification : Model() {
            abstract val title: String
            abstract val body: String

            data class Simple(
                override val title: String,
                override val body: String,
            ) : Message()

            data class Decrypted(
                override val title: String,
                override val body: String,
                val icon: String?,
                val url: String?,
                val type: String,
                val topic: String,
            ) : Message()
        }

        @Deprecated("We renamed data class to NotificationRecord for consistency")
        data class MessageRecord(val id: String, val topic: String, val publishedAt: Long, val message: Message) : Model()

        data class NotificationRecord(val id: String, val topic: String, val publishedAt: Long, val message: Notification, val metadata: Core.Model.AppMetaData) : Model()

        data class Subscription(
            val topic: String,
            val account: String,
            val relay: Relay,
            val metadata: Core.Model.AppMetaData,
            val scope: Map<ScopeId, ScopeSetting>,
            val expiry: Long,
        ) : Model() {

            data class Relay(val protocol: String, val data: String?)

            @JvmInline
            value class ScopeId(val value: String)

            data class ScopeSetting(val name: String, val description: String, val enabled: Boolean)
        }

        object Cacao : Model() {
            @Keep
            data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface

            data class Payload(
                val iss: String, val domain: String, val aud: String, val version: String, val nonce: String, val iat: String,
                val nbf: String?, val exp: String?, val statement: String?, val requestId: String?, val resources: List<String>?,
            ) : Model()
        }

        data class NotificationType(val id: String, val name: String, val description: String) : Model()

        data class Error(val throwable: Throwable) : Model()

        data class CacaoPayloadWithIdentityPrivateKey(val payload: Cacao.Payload, val key: PrivateKey) : Model()
    }

    sealed class Event {

        @Deprecated("We renamed data class to Notification for consistency")
        data class Message(val message: Model.MessageRecord) : Event()

        data class Notification(val notification: Model.NotificationRecord) : Event()

        data class Delete(val topic: String) : Event()

        sealed class Subscription : Event() {

            data class Result(val subscription: Model.Subscription) : Subscription()

            data class Error(val id: Long, val reason: String) : Subscription()
        }

        sealed class Update : Event() {

            data class Result(val subscription: Model.Subscription) : Update()

            data class Error(val id: Long, val reason: String) : Update()
        }

        data class SubscriptionsChanged(val subscriptions: List<Model.Subscription>) : Event()
    }

    sealed class Params {

        class Init(val core: CoreInterface) : Params()

        data class Subscribe(val appDomain: Uri, val account: String) : Params()

        data class Update(val topic: String, val scope: List<String>) : Params()

        data class NotificationTypes(val appDomain: String) : Params()

        @Deprecated("We renamed function to getNotificationHistory for consistency")
        data class MessageHistory(val topic: String) : Params()

        data class NotificationHistory(val topic: String) : Params()

        data class DeleteSubscription(val topic: String) : Params()

        @Deprecated("We renamed function to deleteNotification for consistency")
        data class DeleteMessage(val id: Long) : Params()

        data class DeleteNotification(val id: Long) : Params()

        @Deprecated("We renamed function to decryptNotification for consistency")
        data class DecryptMessage(val topic: String, val encryptedMessage: String) : Params()

        data class DecryptNotification(val topic: String, val encryptedMessage: String) : Params()

        @Deprecated("We changed the registration flow to be more secure. Please use PrepareRegistration and Register instead")
        data class Registration(val account: String, val domain: String, val onSign: (String) -> Model.Cacao.Signature?, val isLimited: Boolean = false) : Params()

        data class PrepareRegistration(val account: String, val domain: String, val allApps: Boolean = false) : Params()

        data class Register(val cacaoPayloadWithIdentityPrivateKey: Model.CacaoPayloadWithIdentityPrivateKey, val signature: Model.Cacao.Signature) : Params()

        data class IsRegistered(val account: String, val domain: String, val allApps: Boolean = false) : Params()

        data class Unregistration(val account: String) : Params()
    }
}