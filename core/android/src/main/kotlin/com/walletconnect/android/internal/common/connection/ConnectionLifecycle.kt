package com.walletconnect.android.internal.common.connection

import kotlinx.coroutines.flow.StateFlow

interface ConnectionLifecycle {
    val onResume: StateFlow<Boolean?>
    fun reconnect()
}