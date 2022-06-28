@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.network

import com.walletconnect.chat.copiedFromSign.core.model.client.WalletConnect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface Relay {
    val eventsFlow: SharedFlow<WalletConnect.Model.Relay.Event>

    val subscriptionRequest: Flow<WalletConnect.Model.Relay.Call.Subscription.Request>

    fun connect(onError: (String) -> Unit)

    fun disconnect(onError: (String) -> Unit)

    fun publish(
        topic: String,
        message: String,
        prompt: Boolean = false,
        onResult: (Result<WalletConnect.Model.Relay.Call.Publish.Acknowledgement>) -> Unit = {},
    )

    fun subscribe(topic: String, onResult: (Result<WalletConnect.Model.Relay.Call.Subscribe.Acknowledgement>) -> Unit)

    fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<WalletConnect.Model.Relay.Call.Unsubscribe.Acknowledgement>) -> Unit,
    )
}