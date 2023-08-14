package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.PairUseCase
import com.walletconnect.sign.engine.use_case.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.SessionRequestUseCase
import com.walletconnect.sign.engine.use_case.SessionUpdateUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callsModule() = module {

    single { ProposeSessionUseCase(get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { PairUseCase(get()) }

    single { ApproveSessionUseCase(get(), get(), get(), get(), get(), get(), get()) }

    single { RejectSessionUseCase(get(), get(), get()) }

    single { SessionUpdateUseCase(get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { SessionRequestUseCase(get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { RespondSessionRequestUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER)), get()) }
}