package com.walletconnect.walletconnectv2.network.model

import com.tinder.scarlet.WebSocket

internal sealed class RelayEvent {
    data class OnConnectionOpened<out WEB_SOCKET : Any>(val webSocket: WEB_SOCKET) : RelayEvent()
    data class OnMessageReceived(val message: RelayMessage) : RelayEvent()
    data class OnConnectionClosing(val shutdownReason: RelayShutdownReason) : RelayEvent()
    data class OnConnectionClosed(val shutdownReason: RelayShutdownReason) : RelayEvent()
    data class OnConnectionFailed(val throwable: Throwable) : RelayEvent()
}

internal fun WebSocket.Event.toRelayEvent() = when (this) {
    is WebSocket.Event.OnConnectionOpened<*> -> RelayEvent.OnConnectionOpened(webSocket)
    is WebSocket.Event.OnMessageReceived -> RelayEvent.OnMessageReceived(message.toRelayMessage())
    is WebSocket.Event.OnConnectionClosing -> RelayEvent.OnConnectionClosing(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionClosed -> RelayEvent.OnConnectionClosed(shutdownReason.toRelayShutdownReason())
    is WebSocket.Event.OnConnectionFailed -> RelayEvent.OnConnectionFailed(throwable)
}


