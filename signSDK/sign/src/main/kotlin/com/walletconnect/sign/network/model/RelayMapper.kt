package com.walletconnect.sign.network.model

import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.WebSocket
import com.walletconnect.sign.core.model.client.WalletConnect

@JvmSynthetic
internal fun WebSocket.Event.toRelayEvent() = when (this) {
    is WebSocket.Event.OnConnectionOpened<*> ->
        WalletConnect.Model.Relay.Event.OnConnectionOpened(webSocket)
    is WebSocket.Event.OnMessageReceived ->
        WalletConnect.Model.Relay.Event.OnMessageReceived(message.toRelayMessage())
    is WebSocket.Event.OnConnectionClosing ->
        WalletConnect.Model.Relay.Event.OnConnectionClosing(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionClosed ->
        WalletConnect.Model.Relay.Event.OnConnectionClosed(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionFailed ->
        WalletConnect.Model.Relay.Event.OnConnectionFailed(throwable)
}

@JvmSynthetic
internal fun Message.toRelayMessage() = when (this) {
    is Message.Text -> WalletConnect.Model.Relay.Message.Text(value)
    is Message.Bytes -> WalletConnect.Model.Relay.Message.Bytes(value)
}

@JvmSynthetic
internal fun ShutdownReason.toRelayShutdownReason() =
    WalletConnect.Model.Relay.ShutdownReason(code, reason)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.SubscriptionData.toRelaySubscriptionData() =
    WalletConnect.Model.Relay.Call.Subscription.Request.Params.SubscriptionData(topic.value, message)

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.Params.toRelayParams() =
    WalletConnect.Model.Relay.Call.Subscription.Request.Params(subscriptionId.id, subscriptionData.toRelaySubscriptionData())

@JvmSynthetic
internal fun RelayDTO.Subscription.Request.toRelayRequest() =
    WalletConnect.Model.Relay.Call.Subscription.Request(id, jsonrpc, method, params.toRelayParams())

@JvmSynthetic
internal fun RelayDTO.Publish.Acknowledgement.toRelayAcknowledgment() =
    WalletConnect.Model.Relay.Call.Publish.Acknowledgement(id, jsonrpc, result)

@JvmSynthetic
internal fun RelayDTO.Subscribe.Acknowledgement.toRelayAcknowledgment() =
    WalletConnect.Model.Relay.Call.Subscribe.Acknowledgement(id, jsonrpc, result.id)

@JvmSynthetic
internal fun RelayDTO.Unsubscribe.Acknowledgement.toRelayAcknowledgment() =
    WalletConnect.Model.Relay.Call.Unsubscribe.Acknowledgement(id, jsonrpc, result)