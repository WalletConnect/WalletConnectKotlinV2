package com.walletconnect.chatsample.ui.shared

import androidx.annotation.DrawableRes

data class ChatUI(@DrawableRes val icon: Int, val username: String, val lastMessage: String, val id: Long?)
