package com.walletconnect.android.internal.common.di

import org.koin.dsl.module

@JvmSynthetic
internal fun verifyModule(verifyUrl: String?) = module {
    //todo: init verify module
}

private const val VERIFY_SERVER = "https://verify.walletconnect.com/"