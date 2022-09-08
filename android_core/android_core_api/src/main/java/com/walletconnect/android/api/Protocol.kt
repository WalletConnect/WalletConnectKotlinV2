package com.walletconnect.android.api

import com.walletconnect.android.api.di.mutex
import com.walletconnect.android.api.di.protocolScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

abstract class Protocol {
    protected abstract fun checkEngineInitialization()
//    protected val mutex = Mutex()
//    private val protocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    protected fun <T> awaitLock(codeBlock: suspend () -> T): T {
        return runBlocking(protocolScope.coroutineContext) {
            mutex.withLock {
                checkEngineInitialization()
                codeBlock()
            }
        }
    }
}