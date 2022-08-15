@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toEngineAppMetaData
import com.walletconnect.sign.engine.domain.SignEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Sign.Model.AppMetaData) = module {

    single {
        metadata.toEngineAppMetaData()
    }

    single {
        SignEngine(get(), get(), get(), get())
    }
}