package com.walletconnect.auth.di

import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.domain.GetResponseByIdUseCase
import com.walletconnect.auth.signature.cacao.CacaoVerifier
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(issuer: String?) = module {

    single { GetPendingJsonRpcHistoryEntriesUseCase(get(), get()) }
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }
    single { GetResponseByIdUseCase(get(), get()) }

    single { CacaoVerifier(get()) }

    if (issuer != null) {
        single { issuer.toCommon() }
        single {
            AuthEngine(get(), get(), get(), get(), get(), get(), get(), get(), get())
        }
    } else {
        single { AuthEngine(get(), get(), get(), get(), get(), get(), get(), null, get()) }
    }
}