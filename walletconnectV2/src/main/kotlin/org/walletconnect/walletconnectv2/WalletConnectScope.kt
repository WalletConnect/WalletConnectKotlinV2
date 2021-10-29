package org.walletconnect.walletconnectv2

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

object WalletConnectScope {
    private val job = SupervisorJob()
    internal val scope = CoroutineScope(job + Dispatchers.IO)

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.tag("WalletConnect exception").e(exception)
    }
}