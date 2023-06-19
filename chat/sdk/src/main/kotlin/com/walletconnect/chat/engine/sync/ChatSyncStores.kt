package com.walletconnect.chat.engine.sync

private const val CHAT_STORE_PREFIX = "com.walletconnect.chat."

enum class ChatSyncStores(val value: String) {
    CHAT_SENT_INVITES(CHAT_STORE_PREFIX + "sentInvites"),
    CHAT_THREADS(CHAT_STORE_PREFIX + "threads"),
    CHAT_INVITE_KEYS(CHAT_STORE_PREFIX + "inviteKeys"),
    CHAT_RECEIVED_INVITE_STATUSES(CHAT_STORE_PREFIX + "receivedInviteStatuses");
}