package com.walletconnect.auth.di


import com.walletconnect.android.impl.di.coreStorageModule
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {
    includes(coreStorageModule())
}