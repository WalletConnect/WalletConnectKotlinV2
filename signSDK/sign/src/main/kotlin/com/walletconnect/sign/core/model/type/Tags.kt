package com.walletconnect.sign.core.model.type

// Definition of tags
// https://github.com/WalletConnect/walletconnect-specs/blob/main/relay/relay-server.md#definitions
enum class Tags(val id: Int) {
    UNKNOWN(0), SIGN(1), CHAT(2), AUTH(3), PUSH(4)
}