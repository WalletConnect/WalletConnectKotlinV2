@file:JvmSynthetic

package com.walletconnect.sign.network

import com.walletconnect.sign.client.Sign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface Relay {
    val eventsFlow: SharedFlow<Sign.Model.Relay.Event>

    val subscriptionRequest: Flow<Sign.Model.Relay.Call.Subscription.Request>

    fun connect(onError: (String) -> Unit)

    fun disconnect(onError: (String) -> Unit)

    fun publish(
        topic: String,
        message: String,
        prompt: Boolean = false,
        onResult: (Result<Sign.Model.Relay.Call.Publish.Acknowledgement>) -> Unit = {},
    )

    fun subscribe(topic: String, onResult: (Result<Sign.Model.Relay.Call.Subscribe.Acknowledgement>) -> Unit)

    fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<Sign.Model.Relay.Call.Unsubscribe.Acknowledgement>) -> Unit,
    )
}