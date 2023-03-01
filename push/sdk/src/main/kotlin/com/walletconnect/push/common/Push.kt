package com.walletconnect.push.common

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient

object Push {

    sealed class Model {

        data class Message(val title: String, val body: String, val icon: String?, val url: String?): Wallet.Event()

        data class MessageRecord(val id: String, val topic: String, val publishedAt: Long, val message: Model.Message): Wallet.Event()

        data class Subscription(val requestId: Long, val topic: String, val account: String, val relay: Relay, val metadata: Core.Model.AppMetaData?): Model() {

            data class Relay(val protocol: String, val data: String?)
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

            data class Message(val title: String, val body: String, val icon: String?, val url: String?): Event()

            data class Delete(val topic: String): Event()
        }

        sealed class Params {

            class Init(val core: CoreClient): Params()

            data class Approve(val id: Long): Params()

            data class Reject(val id: Long, val reason: String): Params()

            data class MessageHistory(val topic: String): Params()

            data class DeleteSubscription(val topic: String): Params()

            data class DeleteMessage(val id: Long): Params()

            data class DecryptMessage(val topic: String, val encryptedMessage: String): Params()
        }
    }
}