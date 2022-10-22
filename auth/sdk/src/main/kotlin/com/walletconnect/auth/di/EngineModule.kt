package com.walletconnect.auth.di

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.model.AppMetaData
import com.walletconnect.auth.common.model.Issuer
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.signature.cacao.CacaoVerifier
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Auth.Model.AppMetaData, issuer: String?) = module {

    single<AppMetaData> { metadata.toCommon() }

    single<CacaoVerifier> { CacaoVerifier(get())  }

    if (issuer != null) {
        single<Issuer> { issuer.toCommon() }

        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), get(), get()) }
    } else {
        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), null, get()) }
    }
}