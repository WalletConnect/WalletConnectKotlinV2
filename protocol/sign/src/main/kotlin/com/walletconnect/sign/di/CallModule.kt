package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCase
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCase
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCase
import com.walletconnect.sign.engine.use_case.calls.PairUseCase
import com.walletconnect.sign.engine.use_case.calls.PingUseCase
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCase
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

    single { PingUseCase(get(), get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { EmitEventUseCase(get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { ExtendSessionUsesCase(get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { DisconnectSessionUseCase(get(), get(), get(named(AndroidCommonDITags.LOGGER))) }

    single { GetSessionsUseCase(get(), get(), get()) }

    single { GetPairingsUseCase(get()) }

    single { GetPendingRequestsUseCaseByTopic(get(), get()) }

    single { GetPendingSessionRequestByTopicUseCase(get(), get(), get()) }

    single { GetPendingJsonRpcHistoryEntryByIdUseCase(get(), get()) }

    single { GetSessionProposalsUseCase(get()) }

    single { GetVerifyContextByIdUseCase(get()) }

    single { GetListOfVerifyContextsUseCase(get()) }
}