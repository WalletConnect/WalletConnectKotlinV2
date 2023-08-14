package com.walletconnect.auth.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.auth.use_case.FormatMessageUseCase
import com.walletconnect.auth.use_case.RespondAuthRequestUseCase
import com.walletconnect.auth.use_case.SendAuthRequestUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun callsModule() = module {

    single { SendAuthRequestUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { RespondAuthRequestUseCase(get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { FormatMessageUseCase() }
}