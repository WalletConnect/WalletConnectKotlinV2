package com.walletconnect.android.api.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import org.koin.core.KoinApplication
import java.util.concurrent.Executors

var wcKoinApp: KoinApplication = KoinApplication.init() //todo: to singletone not to leak wcKoinApp

val mutex = Mutex()
val protocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
