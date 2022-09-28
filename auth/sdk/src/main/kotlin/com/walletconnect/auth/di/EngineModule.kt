package com.walletconnect.auth.di

import com.walletconnect.android.Core
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.model.AppMetaData
import com.walletconnect.auth.common.model.Issuer
import com.walletconnect.auth.engine.domain.AuthEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Core.Model.AppMetaData, issuer: String?) = module {

    single<AppMetaData> { metadata.toCommon() }

    if (issuer != null) {
        single<Issuer> { issuer.toCommon() }

        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), get()) }
    } else {
        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), null) }
    }
}