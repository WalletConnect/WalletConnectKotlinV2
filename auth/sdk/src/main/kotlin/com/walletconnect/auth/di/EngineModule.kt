package com.walletconnect.auth.di

import com.walletconnect.android.Core
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.model.AppMetaData
import com.walletconnect.auth.common.model.Issuer
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.domain.GetResponseByIdUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Core.Model.AppMetaData, issuer: String?) = module {

    single<AppMetaData> { metadata.toCommon() }

    //todo: check if dependencies are here
    single { GetPendingJsonRpcHistoryEntriesUseCase(get(), get()) }
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }
    single { GetResponseByIdUseCase(get(), get()) }

    if (issuer != null) {
        single<Issuer> { issuer.toCommon() }

        //todo: check if dependencies are here
        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    } else {
        //todo: check if dependencies are here
        single<AuthEngine> { AuthEngine(get(), get(), get(), get(), get(), get(), get(), get(), null) }
    }
}