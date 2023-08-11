@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(callsModule(), requestsModule(), responsesModule())

    single { GetPendingRequestsUseCaseByTopic(get(), get()) }

    single { GetPendingSessionRequests(get(), get()) }

    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }

    single { SignEngine(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(named(AndroidCommonDITags.LOGGER)), get()) }
}