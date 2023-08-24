package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCase
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCase
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUsesCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetPairingsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionProposalsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetSessionsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCase
import com.walletconnect.sign.engine.use_case.calls.GetVerifyContextByIdUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PairUseCase
import com.walletconnect.sign.engine.use_case.calls.PairUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.PingUseCase
import com.walletconnect.sign.engine.use_case.calls.PingUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ProposeSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionUpdateUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopic
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCaseByTopicInterface
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequestByTopicUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun callsModule() = module {

    single<ProposeSessionUseCaseInterface> {
        ProposeSessionUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            selfAppMetaData = get(),
            proposalStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<PairUseCaseInterface> { PairUseCase(pairingInterface = get()) }

    single<ApproveSessionUseCaseInterface> {
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

    single<RejectSessionUseCaseInterface> { RejectSessionUseCase(verifyContextStorageRepository = get(), proposalStorageRepository = get(), jsonRpcInteractor = get()) }

    single<SessionUpdateUseCaseInterface> { SessionUpdateUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<SessionRequestUseCaseInterface> { SessionRequestUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<RespondSessionRequestUseCaseInterface> {
        RespondSessionRequestUseCase(
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            sessionStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get()
        )
    }

    single<PingUseCaseInterface> { PingUseCase(sessionStorageRepository = get(), jsonRpcInteractor = get(), pairingInterface = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<EmitEventUseCaseInterface> { EmitEventUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<ExtendSessionUsesCaseInterface> { ExtendSessionUsesCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<DisconnectSessionUseCaseInterface> { DisconnectSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<GetSessionsUseCaseInterface> { GetSessionsUseCase(sessionStorageRepository = get(), metadataStorageRepository = get(), selfAppMetaData = get()) }

    single<GetPairingsUseCaseInterface> { GetPairingsUseCase(pairingInterface = get()) }

    single<GetPendingRequestsUseCaseByTopicInterface> { GetPendingRequestsUseCaseByTopic(serializer = get(), jsonRpcHistory = get()) }

    single<GetPendingSessionRequestByTopicUseCaseInterface> { GetPendingSessionRequestByTopicUseCase(jsonRpcHistory = get(), serializer = get(), metadataStorageRepository = get()) }

    single<GetSessionProposalsUseCaseInterface> { GetSessionProposalsUseCase(proposalStorageRepository = get()) }

    single<GetVerifyContextByIdUseCaseInterface> { GetVerifyContextByIdUseCase(verifyContextStorageRepository = get()) }

    single<GetListOfVerifyContextsUseCaseInterface> { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }
}