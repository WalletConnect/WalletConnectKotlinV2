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

    single { SendAuthRequestUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { RespondAuthRequestUseCase(get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { FormatMessageUseCase() }

    single { GetVerifyContextUseCase(get()) }

    single { GetListOfVerifyContextsUseCase(get()) }
}