@file:JvmName("WalletConnectScope")

package org.walletconnect.walletconnectv2

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.walletconnect.walletconnectv2.crypto.KeyChain
import org.walletconnect.walletconnectv2.util.Logger
import timber.log.Timber

//TODO add job cancellation to avoid memory leaks
private val job = SupervisorJob()
internal val scope = CoroutineScope(job + Dispatchers.IO)

internal val exceptionHandler = CoroutineExceptionHandler { _, exception ->
    Logger.error(exception)
}

//TODO provide with DI
internal val keyChain = object : KeyChain {
    val mapOfKeys = mutableMapOf<String, String>()

    override fun setKey(key: String, value: String) {
        mapOfKeys[key] = value
    }

    override fun getKey(key: String): String {
        return mapOfKeys[key]!!
    }
}