package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.domain.GetResponseByIdUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(callsModule(), requestsModule(), responsesModule())

    single { GetPendingJsonRpcHistoryEntriesUseCase(jsonRpcHistory = get(), serializer = get()) }
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcHistory = get(), serializer = get()) }
    single { GetResponseByIdUseCase(serializer = get(), jsonRpcHistory = get()) }
    single { CacaoVerifier(projectId = get()) }
    single {
        AuthEngine(
            jsonRpcInteractor = get(),
            getListOfVerifyContextsUseCase = get(),
            getVerifyContextUseCase = get(),
            formatMessageUseCase = get(),
            onAuthRequestUseCase = get(),
            onAuthRequestResponseUseCase = get(),
            respondAuthRequestUseCase = get(),
            sendAuthRequestUseCase = get(),
            pairingHandler = get(),
            getPendingJsonRpcHistoryEntriesUseCase = get()
        )
    }
}