@file:JvmSynthetic

package com.walletconnect.foundation.common

import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.WebSocket
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.network.model.RelayDTO

@JvmSynthetic
fun WebSocket.Event.toRelayEvent() = when (this) {
    is WebSocket.Event.OnConnectionOpened<*> ->
        Relay.Model.Event.OnConnectionOpened(webSocket)
    is WebSocket.Event.OnMessageReceived ->
        Relay.Model.Event.OnMessageReceived(message.toRelay())
    is WebSocket.Event.OnConnectionClosing ->
        Relay.Model.Event.OnConnectionClosing(shutdownReason.toRelay())
    is WebSocket.Event.OnConnectionClosed ->
        Relay.Model.Event.OnConnectionClosed(shutdownReason.toRelay())
    is WebSocket.Event.OnConnectionFailed ->
        Relay.Model.Event.OnConnectionFailed(throwable)
}

@JvmSynthetic
internal fun Message.toRelay() = when (this) {
    is Message.Text -> Relay.Model.Message.Text(value)
    is Message.Bytes -> Relay.Model.Message.Bytes(value)
}

@JvmSynthetic
internal fun ShutdownReason.toRelay() =
    Relay.Model.ShutdownReason(code, reason)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.SubscriptionData.toRelay() =
    Relay.Model.Call.Subscription.Request.Params.SubscriptionData(topic.value, message, publishedAt, tag)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.toRelay() =
    Relay.Model.Call.Subscription.Request.Params(subscriptionId.id, subscriptionData.toRelay())

@JvmSynthetic
fun RelayDTO.Subscription.Request.toRelay() =
    Relay.Model.Call.Subscription.Request(id, jsonrpc, method, params.toRelay())

@JvmSynthetic
fun RelayDTO.Publish.Result.Acknowledgement.toRelay() =
    Relay.Model.Call.Publish.Acknowledgement(id, jsonrpc, result)

@JvmSynthetic
fun RelayDTO.Subscribe.Result.Acknowledgement.toRelay() =
    Relay.Model.Call.Subscribe.Acknowledgement(id, jsonrpc, result.id)

@JvmSynthetic
fun RelayDTO.BatchSubscribe.Result.Acknowledgement.toRelay() =
    Relay.Model.Call.BatchSubscribe.Acknowledgement(id, jsonrpc, result)

@JvmSynthetic
fun RelayDTO.Unsubscribe.Result.Acknowledgement.toRelay() =
    Relay.Model.Call.Unsubscribe.Acknowledgement(id, jsonrpc, result)