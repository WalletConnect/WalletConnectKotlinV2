package com.walletconnect.android.api.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import org.koin.core.KoinApplication
import java.util.concurrent.Executors
import kotlin.random.Random

var wcKoinApp: KoinApplication = KoinApplication.init().apply { createEagerInstances() } //todo: to singletone not to leak wcKoinApp
val test = Random.nextInt()
val mutex = Mutex()
val protocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
