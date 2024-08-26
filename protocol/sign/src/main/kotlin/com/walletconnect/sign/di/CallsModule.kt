package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionAuthenticateUseCase
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionAuthenticateUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ApproveSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.DecryptSignMessageUseCase
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.DisconnectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCase
import com.walletconnect.sign.engine.use_case.calls.EmitEventUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.ExtendSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.FormatAuthenticateMessageUseCase
import com.walletconnect.sign.engine.use_case.calls.FormatAuthenticateMessageUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCase
import com.walletconnect.sign.engine.use_case.calls.GetListOfVerifyContextsUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.GetNamespacesFromReCaps
import com.walletconnect.sign.engine.use_case.calls.GetPairingForSessionAuthenticateUseCase
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
import com.walletconnect.sign.engine.use_case.calls.RejectSessionAuthenticateUseCase
import com.walletconnect.sign.engine.use_case.calls.RejectSessionAuthenticateUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCase
import com.walletconnect.sign.engine.use_case.calls.RejectSessionUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.RespondSessionRequestUseCaseInterface
import com.walletconnect.sign.engine.use_case.calls.SessionAuthenticateUseCase
import com.walletconnect.sign.engine.use_case.calls.SessionAuthenticateUseCaseInterface
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

    single<SessionAuthenticateUseCaseInterface> {
        SessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            selfAppMetaData = get(),
            authenticateResponseTopicRepository = get(),
            proposeSessionUseCase = get(),
            getPairingForSessionAuthenticate = get(),
            getNamespacesFromReCaps = get(),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>(),
            linkModeStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
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
            verifyContextStorageRepository = get(),
            insertEventUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<ApproveSessionAuthenticateUseCaseInterface> {
        ApproveSessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            cacaoVerifier = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            verifyContextStorageRepository = get(),
            getPendingSessionAuthenticateRequest = get(),
            selfAppMetaData = get(),
            sessionStorageRepository = get(),
            metadataStorageRepository = get(),
            insertTelemetryEventUseCase = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>()
        )
    }

    single<RejectSessionAuthenticateUseCaseInterface> {
        RejectSessionAuthenticateUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            verifyContextStorageRepository = get(),
            getPendingSessionAuthenticateRequest = get(),
            linkModeJsonRpcInteractor = get<LinkModeJsonRpcInteractorInterface>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            insertEventUseCase = get()
        )
    }

    single<RejectSessionUseCaseInterface> {
        RejectSessionUseCase(
            verifyContextStorageRepository = get(),
            proposalStorageRepository = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            pairingController = get()
        )
    }

    single<SessionUpdateUseCaseInterface> { SessionUpdateUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<SessionRequestUseCaseInterface> {
        SessionRequestUseCase(
            jsonRpcInteractor = get(),
            sessionStorageRepository = get(),
            linkModeJsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single<RespondSessionRequestUseCaseInterface> {
        RespondSessionRequestUseCase(
            jsonRpcInteractor = get(),
            verifyContextStorageRepository = get(),
            sessionStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            getPendingJsonRpcHistoryEntryByIdUseCase = get(),
            linkModeJsonRpcInteractor = get(),
            metadataStorageRepository = get(),
            insertEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
        )
    }

    single<DecryptMessageUseCaseInterface>(named(AndroidCommonDITags.DECRYPT_SIGN_MESSAGE)) {
        val useCase = DecryptSignMessageUseCase(
            codec = get(),
            serializer = get(),
            metadataRepository = get(),
            pushMessageStorage = get(),
        )

        get<MutableMap<String, DecryptMessageUseCaseInterface>>(named(AndroidCommonDITags.DECRYPT_USE_CASES))[Tags.SESSION_PROPOSE.id.toString()] = useCase
        useCase
    }

    single<PingUseCaseInterface> { PingUseCase(sessionStorageRepository = get(), jsonRpcInteractor = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<EmitEventUseCaseInterface> { EmitEventUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<ExtendSessionUseCaseInterface> { ExtendSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<DisconnectSessionUseCaseInterface> { DisconnectSessionUseCase(jsonRpcInteractor = get(), sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single<GetSessionsUseCaseInterface> { GetSessionsUseCase(sessionStorageRepository = get(), metadataStorageRepository = get(), selfAppMetaData = get()) }

    single<GetPairingsUseCaseInterface> { GetPairingsUseCase(pairingInterface = get()) }

    single { GetPairingForSessionAuthenticateUseCase(pairingProtocol = get()) }

    single { GetNamespacesFromReCaps() }

    single<GetPendingRequestsUseCaseByTopicInterface> { GetPendingRequestsUseCaseByTopic(serializer = get(), jsonRpcHistory = get()) }

    single<GetPendingSessionRequestByTopicUseCaseInterface> { GetPendingSessionRequestByTopicUseCase(jsonRpcHistory = get(), serializer = get(), metadataStorageRepository = get()) }

    single<GetSessionProposalsUseCaseInterface> { GetSessionProposalsUseCase(proposalStorageRepository = get()) }

    single<GetVerifyContextByIdUseCaseInterface> { GetVerifyContextByIdUseCase(verifyContextStorageRepository = get()) }

    single<GetListOfVerifyContextsUseCaseInterface> { GetListOfVerifyContextsUseCase(verifyContextStorageRepository = get()) }

    single<FormatAuthenticateMessageUseCaseInterface> { FormatAuthenticateMessageUseCase() }
}