package com.walletconnect.chatsample.ui

import androidx.annotation.DrawableRes

sealed class ThreadUI {

    data class Self(val message: String): ThreadUI()

    data class Peer(@DrawableRes val icon: Int, val message: String): ThreadUI()
}
