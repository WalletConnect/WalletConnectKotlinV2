package com.walletconnect.chat.copiedFromSign.json_rpc.model

//TODO: Should there be a copy for every SDK with specific methods or one file with all methods.
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