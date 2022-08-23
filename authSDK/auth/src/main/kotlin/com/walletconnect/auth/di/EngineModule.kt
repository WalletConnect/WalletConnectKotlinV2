package com.walletconnect.auth.di

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toEngineDO
import com.walletconnect.auth.engine.domain.AuthEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Auth.Model.AppMetaData) = module {

    single { metadata.toEngineDO() }

    single { AuthEngine(get(), get(), get()) }
}