package com.walletconnect.web3.modal.di

import com.walletconnect.web3.modal.ui.components.internal.email.webview.EmailProvider
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

internal fun emailModule() = module {
    single {
        EmailProvider(
            context = androidContext(),
            logger = get()
        )
    }
}