package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntriesUseCaseInterface
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.auth.json_rpc.domain.GetPendingJsonRpcHistoryEntryByTopicUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(callsModule(), requestsModule(), responsesModule())

    single<GetPendingJsonRpcHistoryEntriesUseCaseInterface> { GetPendingJsonRpcHistoryEntriesUseCase(jsonRpcHistory = get(), serializer = get()) }
    single { GetPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcHistory = get(), serializer = get()) }
    single { GetPendingJsonRpcHistoryEntryByTopicUseCase(jsonRpcHistory = get(), serializer = get()) }
    single { CacaoVerifier(projectId = get()) }
    single {
        AuthEngine(
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            getListOfVerifyContextsUseCase = get(),
            getVerifyContextUseCase = get(),
            formatMessageUseCase = get(),
            onAuthRequestUseCase = get(),
            onAuthRequestResponseUseCase = get(),
            respondAuthRequestUseCase = get(),
            sendAuthRequestUseCase = get(),
            pairingHandler = get(),
            getPendingJsonRpcHistoryEntriesUseCase = get(),
            getPendingJsonRpcHistoryEntryByTopicUseCase = get(),
            decryptAuthMessageUseCase = get(named(AndroidCommonDITags.DECRYPT_AUTH_MESSAGE)),
        )
    }
}