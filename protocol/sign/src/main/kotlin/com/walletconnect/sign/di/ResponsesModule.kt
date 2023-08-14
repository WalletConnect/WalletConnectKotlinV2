package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.responses.OnSessionProposalResponseUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun responsesModule() = module {

    single { OnSessionProposalResponseUseCase(get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }
}