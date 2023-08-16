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

    single { ProposeSessionUseCase(jsonRpcInteractor = get(), crypto = get(), selfAppMetaData = get(), proposalStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { PairUseCase(pairingInterface = get()) }

    single {
        ApproveSessionUseCase(
            proposalStorageRepository = get(),
            selfAppMetaData = get(),
            crypto = get(),
            jsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            sessionStorageRepository = get(),
            verifyContextStorageRepository = get()
        )
    }

    single { RejectSessionUseCase(verifyContextStorageRepository = get(), proposalStorageRepository = get(), jsonRpcInteractor = get()) }

    single { SessionUpdateUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { SessionRequestUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single {
        RespondSessionRequestUseCase(
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            sessionStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get()
        )
    }

    single { PingUseCase(sessionStorageRepository = get(), jsonRpcInteractor = get(), pairingInterface = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { EmitEventUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { ExtendSessionUsesCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { DisconnectSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single { GetSessionsUseCase(sessionStorageRepository = get(), metadataStorageRepository = get(), selfAppMetaData = get()) }

    single { GetPairingsUseCase(pairingInterface = get()) }

    single { GetPendingRequestsUseCaseByTopic(serializer = get(), jsonRpcHistory = get()) }

    single { GetPendingSessionRequestByTopicUseCase(jsonRpcHistory = get(), serializer = get(), metadataStorageRepository = get()) }

    single { GetPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcHistory = get(), serializer = get()) }

    single { GetSessionProposalsUseCase(proposalStorageRepository = get()) }

    single { GetVerifyContextByIdUseCase(verifyContextStorageRepository = get()) }

    single { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }
}