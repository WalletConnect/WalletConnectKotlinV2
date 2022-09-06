package com.walletconnect.android_core.common.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import java.util.concurrent.Executors

abstract class Protocol {
    protected abstract fun checkEngineInitialization()
    protected val mutex = Mutex()
    protected val protocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    protected val wcKoinApp: KoinApplication = KoinApplication.init()

    init {
        wcKoinApp.modules(initialModules())
    }

    protected fun <T> awaitLock(codeBlock: suspend () -> T): T {
        return runBlocking(protocolScope.coroutineContext) {
            mutex.withLock {
                checkEngineInitialization()
                codeBlock()
            }
        }
    }

    protected abstract fun initialModules(): List<Module>
}