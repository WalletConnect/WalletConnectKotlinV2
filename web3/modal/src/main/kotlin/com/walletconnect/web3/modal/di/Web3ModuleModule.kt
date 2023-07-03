package com.walletconnect.web3.modal.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.domain.configuration.EncodedStringAdapter
import com.walletconnect.web3.modal.domain.configuration.configAdapter

import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val WEB3MODAL_MOSHI = "web3modal_moshi"

internal fun web3ModalModule() = module {

    single(named(WEB3MODAL_MOSHI)) {
        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
            .add(configAdapter())
            .add(EncodedStringAdapter())
            .build()
    }
}
