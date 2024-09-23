package com.walletconnect.notify.client

import android.net.Uri
import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.foundation.common.model.PrivateKey
import kotlin.time.Duration

@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
object Notify {

    sealed class Model {
        sealed class Notification : Model() {
            abstract val title: String
            abstract val body: String

            data class Simple(
                override val title: String,
                override val body: String,
            ) : Notification()

            data class Decrypted(
                override val title: String,
                override val body: String,
                val url: String?,
                val type: String,
                val topic: String,
            ) : Notification()
        }

        data class NotificationRecord(val id: String, val topic: String, val sentAt: Long, val notification: Notification, val metadata: Core.Model.AppMetaData) : Model()

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

        data class NotificationType(val id: String, val name: String, val description: String, val iconUrl: String?) : Model()

        data class Error(val throwable: Throwable) : Model()

        data class CacaoPayloadWithIdentityPrivateKey(val payload: Cacao.Payload, val key: PrivateKey) : Model()
    }

    sealed class Event {
        data class Notification(val notification: Model.NotificationRecord) : Event()

        data class SubscriptionsChanged(val subscriptions: List<Model.Subscription>) : Event()
    }

    sealed interface Result {
        sealed interface Subscribe {
            data class Success(val subscription: Model.Subscription) : Subscribe
            data class Error(val error: Model.Error) : Subscribe
        }

        sealed interface UpdateSubscription {
            data class Success(val subscription: Model.Subscription) : UpdateSubscription
            data class Error(val error: Model.Error) : UpdateSubscription
        }

        sealed interface DeleteSubscription {
            data class Success(val topic: String) : DeleteSubscription
            data class Error(val error: Model.Error) : DeleteSubscription
        }

        sealed interface GetNotificationHistory {
            data class Success(val notifications: List<Model.NotificationRecord>, val hasMore: Boolean) : GetNotificationHistory
            data class Error(val error: Model.Error) : GetNotificationHistory
        }
    }

    sealed class Params {

        class Init(val core: CoreInterface) : Params()

        data class Subscribe(val appDomain: Uri, val account: String, val timeout: Duration? = null) : Params()

        data class UpdateSubscription(val topic: String, val scope: List<String>, val timeout: Duration? = null) : Params()

        data class GetNotificationTypes(val appDomain: String, val timeout: Duration? = null) : Params()

        data class GetNotificationHistory(val topic: String, val limit: Int? = null, val startingAfter: String? = null, val timeout: Duration? = null) : Params()

        data class DeleteSubscription(val topic: String, val timeout: Duration? = null) : Params()

        data class GetActiveSubscriptions(val account: String, val timeout: Duration? = null) : Params()

        data class DecryptNotification(val topic: String, val encryptedMessage: String) : Params()

        data class PrepareRegistration(val account: String, val domain: String) : Params()

        data class Register(val cacaoPayloadWithIdentityPrivateKey: Model.CacaoPayloadWithIdentityPrivateKey, val signature: Model.Cacao.Signature) : Params()

        data class IsRegistered(val account: String, val domain: String) : Params()

        data class Unregister(val account: String) : Params()
    }
}