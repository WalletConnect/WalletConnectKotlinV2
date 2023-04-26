package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.domain.GetResponseByIdUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { GetPendingJsonRpcHistoryEntriesUseCase(get(), get()) }
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }
    single { GetResponseByIdUseCase(get(), get()) }
    single { CacaoVerifier(get()) }
    single { AuthEngine(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }
}