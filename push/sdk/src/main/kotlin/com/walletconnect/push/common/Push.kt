package com.walletconnect.push.common

import android.net.Uri
import androidx.annotation.Keep
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.cacao.SignatureInterface
import java.net.URL

object Push {

    sealed class Model {

        data class Message(val title: String, val body: String, val icon: String?, val url: String?, val type: String): Model()

        data class MessageRecord(val id: String, val topic: String, val publishedAt: Long, val message: Message): Model()

        data class Subscription(val requestId: Long, val topic: String, val account: String, val relay: Relay, val metadata: Core.Model.AppMetaData, val scope: Map<ScopeName, ScopeSetting>, val expiry: Long): Model() {

            data class Relay(val protocol: String, val data: String?)

            @JvmInline
            value class ScopeName(val value: String)

            data class ScopeSetting(val description: String, val enabled: Boolean)
        }

        object Cacao : Model() {
            @Keep
            data class Signature(override val t: String, override val s: String, override val m: String? = null) : Model(), SignatureInterface
        }

        data class Error(val throwable: Throwable) : Model()
    }

    class Dapp {

        sealed class Event {

            data class Response(val subscription: Push.Model.Subscription): Event()

            data class Rejected(val reason: String): Event()

            data class Delete(val topic: String): Event()
        }

        sealed class Model {

            data class RequestId(val id: Long): Model()
        }

        sealed class Params {

            data class Init(val core: CoreClient, val castUrl: String?): Params()

            data class Request(val account: String, val pairingTopic: String): Params()

            data class Notify(val topic: String, val message: Push.Model.Message): Params()

            data class Delete(val topic: String): Params()
        }
    }

    class Wallet {

        sealed class Event {

            data class Request(val id: Long, val metadata: Core.Model.AppMetaData): Event()

            data class Message(val id: String, val topic: String, val publishedAt: Long, val message: Model.Message): Event()

            data class Delete(val topic: String): Event()

            data class Subscription(val subscription: Model.Subscription): Event()
        }

        sealed class Params {

            class Init(val core: CoreClient): Params()

            data class Approve(val id: Long, val onSign: (String) -> Model.Cacao.Signature?): Params()

            data class Reject(val id: Long, val reason: String): Params()

            data class Subscribe(val dappUrl: Uri, val account: String, val onSign: (String) -> Model.Cacao.Signature?): Params()

            data class Update(val topic: String, val scope: List<String>): Params()

            data class MessageHistory(val topic: String): Params()

            data class DeleteSubscription(val topic: String): Params()

            data class DeleteMessage(val id: Long): Params()

            data class DecryptMessage(val topic: String, val encryptedMessage: String): Params()
        }
    }
}