package com.walletconnect.chatsample

import androidx.annotation.DrawableRes

data class ChatUI(@DrawableRes val icon: Int, val username: String, val lastMessage: String, val id: Long?)
