package org.walletconnect.walletconnectv2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object WalletConnectScope {
    private val job = SupervisorJob()
    internal val scope = CoroutineScope(job + Dispatchers.IO)
}