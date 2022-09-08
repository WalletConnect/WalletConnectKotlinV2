package com.walletconnect.android.api.di

import org.koin.core.KoinApplication

var wcKoinApp: KoinApplication = KoinApplication.init().apply { createEagerInstances() }