package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.responses.OnSessionProposalResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionRequestResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionSettleResponseUseCase
import com.walletconnect.sign.engine.use_case.responses.OnSessionUpdateResponseUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun responsesModule() = module {

    single { OnSessionProposalResponseUseCase(get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionSettleResponseUseCase(get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionUpdateResponseUseCase(get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { OnSessionRequestResponseUseCase() }
}