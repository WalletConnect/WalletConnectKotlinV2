package com.walletconnect.android.internal.common

import org.koin.core.KoinApplication

var wcKoinApp: KoinApplication = KoinApplication.createNewWCKoinApp()

// Note: Since `wcKoinApp` is used a lot as a variable this might introduce weird state in clients that have called `CoreProtocol.injectNewWCKoinApp`
fun KoinApplication.Companion.createNewWCKoinApp(): KoinApplication = KoinApplication.init().apply { createEagerInstances() }