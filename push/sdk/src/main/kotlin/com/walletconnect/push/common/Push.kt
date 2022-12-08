package com.walletconnect.push.common

import com.walletconnect.android.CoreClient

object Push {

    sealed class Event {

        data class Response(val error: String?, val subscription: Model.Subscription?): Event()
    }

    sealed class Model {

        data class Message(val title: String, val body: String, val icon: String, val url: String): Model()

        data class Subscription(val t: Int): Model()

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