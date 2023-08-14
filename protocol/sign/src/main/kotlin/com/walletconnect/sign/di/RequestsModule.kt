package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.requests.OnPingUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionDeleteUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionEventUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionExtendUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionProposeUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionSettleUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionUpdateUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun requestsModule() = module {

    single { OnSessionProposeUseCase(get(), get(), get(), get(), get()) }

    single { OnSessionSettleUseCase(get(), get(), get(), get(), get(), get(), get()) }

    single { OnSessionRequestUseCase(get(), get(), get(), get(), get()) }

    single { OnSessionDeleteUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionEventUseCase(get(), get()) }

    single { OnSessionUpdateUseCase(get(), get()) }

    single { OnSessionExtendUseCase(get(), get()) }

    single { OnPingUseCase(get()) }
}