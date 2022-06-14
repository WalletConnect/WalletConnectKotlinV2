package com.walletconnect.sign.network.model

import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.WebSocket
import com.walletconnect.sign.client.Sign

@JvmSynthetic
internal fun WebSocket.Event.toRelayEvent() = when (this) {
    is WebSocket.Event.OnConnectionOpened<*> ->
        Sign.Model.Relay.Event.OnConnectionOpened(webSocket)
    is WebSocket.Event.OnMessageReceived ->
        Sign.Model.Relay.Event.OnMessageReceived(message.toRelayMessage())
    is WebSocket.Event.OnConnectionClosing ->
        Sign.Model.Relay.Event.OnConnectionClosing(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionClosed ->
        Sign.Model.Relay.Event.OnConnectionClosed(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionFailed ->
        Sign.Model.Relay.Event.OnConnectionFailed(throwable)
}

@JvmSynthetic
internal fun Message.toRelayMessage() = when (this) {
    is Message.Text -> Sign.Model.Relay.Message.Text(value)
    is Message.Bytes -> Sign.Model.Relay.Message.Bytes(value)
}

@JvmSynthetic
internal fun ShutdownReason.toRelayShutdownReason() =
    Sign.Model.Relay.ShutdownReason(code, reason)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.SubscriptionData.toRelaySubscriptionData() =
    Sign.Model.Relay.Call.Subscription.Request.Params.SubscriptionData(topic.value, message)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.toRelayParams() =
    Sign.Model.Relay.Call.Subscription.Request.Params(subscriptionId.id, subscriptionData.toRelaySubscriptionData())

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.toRelayRequest() =
    Sign.Model.Relay.Call.Subscription.Request(id, jsonrpc, method, params.toRelayParams())

@JvmSynthetic
internal fun RelayDTO.Publish.Acknowledgement.toRelayAcknowledgment() =
    Sign.Model.Relay.Call.Publish.Acknowledgement(id, jsonrpc, result)

@JvmSynthetic
internal fun RelayDTO.Subscribe.Acknowledgement.toRelayAcknowledgment() =
    Sign.Model.Relay.Call.Subscribe.Acknowledgement(id, jsonrpc, result.id)

@JvmSynthetic
internal fun RelayDTO.Unsubscribe.Acknowledgement.toRelayAcknowledgment() =
    Sign.Model.Relay.Call.Unsubscribe.Acknowledgement(id, jsonrpc, result)