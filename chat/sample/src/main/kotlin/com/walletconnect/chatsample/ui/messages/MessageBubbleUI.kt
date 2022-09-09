package com.walletconnect.chatsample.ui.messages

import androidx.annotation.DrawableRes

sealed class MessageBubbleUI {
    data class Self(val message: String): MessageBubbleUI()
    data class Peer(@DrawableRes val icon: Int, val message: String): MessageBubbleUI()
}
