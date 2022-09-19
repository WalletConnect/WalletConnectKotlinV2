package com.walletconnect.android.common

import org.koin.core.KoinApplication

var wcKoinApp: KoinApplication = KoinApplication.init().apply { createEagerInstances() }