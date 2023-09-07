package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.auth.use_case.calls.FormatMessageUseCase
import com.walletconnect.auth.use_case.calls.FormatMessageUseCaseInterface
import com.walletconnect.auth.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.auth.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.auth.use_case.calls.GetVerifyContextUseCase
import com.walletconnect.auth.use_case.calls.GetVerifyContextUseCaseInterface
import com.walletconnect.auth.use_case.calls.RespondAuthRequestUseCase
import com.walletconnect.auth.use_case.calls.RespondAuthRequestUseCaseInterface
import com.walletconnect.auth.use_case.calls.SendAuthRequestUseCase
import com.walletconnect.auth.use_case.calls.SendAuthRequestUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callsModule() = module {

    single<SendAuthRequestUseCaseInterface> { SendAuthRequestUseCase(crypto = get(), jsonRpcInteractor = get(), selfAppMetaData = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<RespondAuthRequestUseCaseInterface> {
        RespondAuthRequestUseCase(
            crypto = get(),
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            cacaoVerifier = get(),
            pairingController = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<FormatMessageUseCaseInterface> { FormatMessageUseCase() }

    single<GetVerifyContextUseCaseInterface> { GetVerifyContextUseCase(verifyContextStorageRepository = get()) }

    single<GetListOfVerifyContextsUseCaseInterface> { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }
}