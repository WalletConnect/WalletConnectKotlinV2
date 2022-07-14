package com.walletconnect.chatsample.ui.messages

data class MessageUI(
    val peerName: String, // Currently id for chats. Normally would be topic
    val text: String,
    val timestamp: Long,
    val author: String,
)