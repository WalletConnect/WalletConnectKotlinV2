package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.auth.use_case.calls.FormatMessageUseCase
import com.walletconnect.auth.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.auth.use_case.calls.GetVerifyContextUseCase
import com.walletconnect.auth.use_case.calls.RespondAuthRequestUseCase
import com.walletconnect.auth.use_case.calls.SendAuthRequestUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun callsModule() = module {

    single { SendAuthRequestUseCase(crypto = get(), jsonRpcInteractor = get(), selfAppMetaData = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single {
        RespondAuthRequestUseCase(
            crypto = get(),
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            cacaoVerifier = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single { FormatMessageUseCase() }

    single { GetVerifyContextUseCase(verifyContextStorageRepository = get()) }

    single { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }
}