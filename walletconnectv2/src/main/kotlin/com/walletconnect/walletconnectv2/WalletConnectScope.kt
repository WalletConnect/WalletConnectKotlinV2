@file:JvmName("WalletConnectScope")

package com.walletconnect.walletconnectv2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

//TODO add job cancellation to avoid memory leaks
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)