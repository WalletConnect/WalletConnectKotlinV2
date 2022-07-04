package com.walletconnect.chat.copiedFromSign.network.model

import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.WebSocket
import com.walletconnect.chat.copiedFromSign.core.model.client.Relay

@JvmSynthetic
internal fun WebSocket.Event.toRelayEvent() = when (this) {
    is WebSocket.Event.OnConnectionOpened<*> ->
        Relay.Model.Event.OnConnectionOpened(webSocket)
    is WebSocket.Event.OnMessageReceived ->
        Relay.Model.Event.OnMessageReceived(message.toRelayMessage())
    is WebSocket.Event.OnConnectionClosing ->
        Relay.Model.Event.OnConnectionClosing(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionClosed ->
        Relay.Model.Event.OnConnectionClosed(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionFailed ->
        Relay.Model.Event.OnConnectionFailed(throwable)
}

@JvmSynthetic
internal fun Message.toRelayMessage() = when (this) {
    is Message.Text -> Relay.Model.Message.Text(value)
    is Message.Bytes -> Relay.Model.Message.Bytes(value)
}

@JvmSynthetic
internal fun ShutdownReason.toRelayShutdownReason() =
    Relay.Model.ShutdownReason(code, reason)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.SubscriptionData.toRelaySubscriptionData() =
    Relay.Model.Call.Subscription.Request.Params.SubscriptionData(topic.value, message)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.toRelayParams() =
    Relay.Model.Call.Subscription.Request.Params(subscriptionId.id, subscriptionData.toRelaySubscriptionData())

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.toRelayRequest() =
    Relay.Model.Call.Subscription.Request(id, jsonrpc, method, params.toRelayParams())

@JvmSynthetic
internal fun RelayDTO.Publish.Acknowledgement.toRelayAcknowledgment() =
    Relay.Model.Call.Publish.Acknowledgement(id, jsonrpc, result)

@JvmSynthetic
internal fun RelayDTO.Subscribe.Acknowledgement.toRelayAcknowledgment() =
    Relay.Model.Call.Subscribe.Acknowledgement(id, jsonrpc, result.id)

@JvmSynthetic
internal fun RelayDTO.Unsubscribe.Acknowledgement.toRelayAcknowledgment() =
    Relay.Model.Call.Unsubscribe.Acknowledgement(id, jsonrpc, result)