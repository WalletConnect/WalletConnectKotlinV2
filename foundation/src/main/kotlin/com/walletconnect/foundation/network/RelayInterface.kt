@file:JvmSynthetic

package com.walletconnect.foundation.network

import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface RelayInterface {
    val eventsFlow: SharedFlow<Relay.Model.Event>

    val subscriptionRequest: Flow<Relay.Model.Call.Subscription.Request>

    fun publish(
        topic: String,
        message: String,
        params: Relay.Model.IrnParams,
        onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit = {},
    )

    fun subscribe(topic: String, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit)

    fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit,
    )
}