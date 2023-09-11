package com.walletconnect.notify.client

import android.net.Uri
import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreInterface
import com.walletconnect.android.cacao.SignatureInterface

object Notify {

    sealed class Model {

        data class Message(val title: String, val body: String, val icon: String?, val url: String?, val type: String) : Model()

        data class MessageRecord(val id: String, val topic: String, val publishedAt: Long, val message: Message) : Model()

        data class Subscription(
            val topic: String,
            val account: String,
            val relay: Relay,
            val metadata: Core.Model.AppMetaData,
            val scope: Map<ScopeName, ScopeSetting>,
            val expiry: Long,
        ) : Model() {

            data class Relay(val protocol: String, val data: String?)

            @JvmInline
            value class ScopeName(val value: String)

            data class ScopeSetting(val description: String, val enabled: Boolean)
        }

        object Cacao : Model() {
            @Keep
            data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
        }

        data class AvailableTypes(val types: List<String>) : Model()

        data class Error(val throwable: Throwable) : Model()
    }

    sealed class Event {

        data class Message(val message: Model.MessageRecord) : Event()

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

        data class Subscribe(val dappUrl: Uri, val account: String) : Params()

        data class Update(val topic: String, val scope: List<String>) : Params()

        data class NotificationTypes(val domain: String) : Params()

        data class MessageHistory(val topic: String) : Params()

        data class DeleteSubscription(val topic: String) : Params()

        data class DeleteMessage(val id: Long) : Params()

        data class DecryptMessage(val topic: String, val encryptedMessage: String) : Params()

        //todo: I kept isLimited as Inbox is an example of native dapp. Bring this to the team and discuss over 11.09.23 - 16.09.23
        data class Registration(val account: String, val isLimited: Boolean, val domain: String, val onSign: (String) -> Model.Cacao.Signature?) : Params()
    }
}