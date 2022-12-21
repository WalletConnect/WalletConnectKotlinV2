package com.walletconnect.push.common

import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.model.AppMetaData

object Push {

    class Dapp {

        sealed class Event {

            data class Response(val error: String?, val subscription: Model.Subscription?): Event()
        }

        sealed class Model {

            data class Message(val title: String, val body: String, val icon: String, val url: String): Model()

            data class Subscription(val topic: String, val relay: Relay, val metadata: AppMetaData): Model() {

                data class Relay(val protocol: String, val `data`: String)
            }

            data class RequestId(val id: Int): Model()

            data class Error(val throwable: Throwable) : Model()
        }

        sealed class Params {

            data class Init(val core: CoreClient, val castUrl: String?): Params()

            data class Request(val account: String, val pairingTopic: String): Params()

            data class Notify(val topic: String, val message: Model.Message): Params()

            data class Delete(val topic: String): Params()
        }
    }

    class Wallet {

        sealed class Model {

            data class Request(val id: Long, val metadata: Core.Model.AppMetaData): Model()

            data class Subscription(val topic: String, val relay: Relay, val metadata: Core.Model.AppMetaData): Model() {

                data class Relay(val protocol: String, val data: String?)
            }

            data class Message(val title: String, val body: String, val icon: String, val url: String): Model()

            data class Error(val throwable: Throwable) : Model()
        }

        sealed class Params {

            class Init(val core: CoreClient): Params()

            data class Approve(val id: Int): Params()

            data class Reject(val id: Int, val reason: String): Params()

            data class Delete(val topic: String): Params()

            data class DecryptMessage(val topic: String, val encryptedMessage: String): Params()
        }
    }
}