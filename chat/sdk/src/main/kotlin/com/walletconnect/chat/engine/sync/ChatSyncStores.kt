package com.walletconnect.chat.engine.sync

private const val CHAT_STORE_PREFIX = "com.walletconnect.web3inbox."

enum class ChatSyncStores(val value: String) {
    CHAT_SENT_INVITES(CHAT_STORE_PREFIX + "chatSentInvites"),
    CHAT_THREADS(CHAT_STORE_PREFIX + "chatThreads"),
    CHAT_INVITE_KEYS(CHAT_STORE_PREFIX + "chatInviteKeys");
}