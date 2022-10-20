package com.walletconnect.chat.json_rpc.model

internal object JsonRpcMethod {
    @get:JvmSynthetic
    const val WC_CHAT_INVITE: String = "wc_chatInvite"
    @get:JvmSynthetic
    const val WC_CHAT_MESSAGE: String = "wc_chatMessage"
    @get:JvmSynthetic
    const val WC_CHAT_PING: String = "wc_chatPing"
    @get:JvmSynthetic
    const val WC_CHAT_LEAVE: String = "wc_chatLeave"
}