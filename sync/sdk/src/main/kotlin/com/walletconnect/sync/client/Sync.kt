package com.walletconnect.sync.client

import com.walletconnect.android.CoreClient

object Sync {
    sealed class Event {}
    sealed class Model {}
    sealed class Params {
        data class Init(val core: CoreClient) : Params()
    }
}