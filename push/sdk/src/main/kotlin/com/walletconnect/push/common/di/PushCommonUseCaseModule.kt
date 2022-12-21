@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.walletconnect.push.common.domain.GetListOfSubscriptionsUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun commonUseCasesModule() = module {

    single { GetListOfSubscriptionsUseCase() }


}