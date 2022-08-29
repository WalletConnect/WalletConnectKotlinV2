package com.walletconnect.auth.di

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toEngineDO
import com.walletconnect.auth.client.mapper.toVO
import com.walletconnect.auth.common.model.IssuerVO
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.engine.model.EngineDO
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Auth.Model.AppMetaData, issuer: String?) = module {

    single<EngineDO.AppMetaData> { metadata.toEngineDO() }

    if (issuer != null) {
        single<IssuerVO> { issuer.toVO() }

        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), get()) }
    } else {
        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), null) }
    }
}