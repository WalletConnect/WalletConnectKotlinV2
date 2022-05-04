package com.walletconnect.walletconnectv2.network.model

import com.tinder.scarlet.Message


internal sealed class RelayMessage {
    data class Text(val value: String) : RelayMessage()
    class Bytes(val value: ByteArray) : RelayMessage() {
        operator fun component1(): ByteArray = value
    }
}

internal fun Message.toRelayMessage() = when (this) {
    is Message.Text -> RelayMessage.Text(value)
    is Message.Bytes -> RelayMessage.Bytes(value)
}
