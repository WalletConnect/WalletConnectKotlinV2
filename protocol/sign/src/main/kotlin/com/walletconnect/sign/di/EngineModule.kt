@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.engine.use_case.calls.GetPendingAuthenticateRequestUseCase
import com.walletconnect.sign.engine.use_case.calls.GetPendingAuthenticateRequestUseCaseInterface
import com.walletconnect.sign.json_rpc.domain.DeleteRequestByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionAuthenticateRequest
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import com.walletconnect.sign.json_rpc.domain.GetSessionAuthenticateRequest
import com.walletconnect.sign.json_rpc.domain.GetSessionRequestByIdUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(callsModule(), requestsModule(), responsesModule())

    single { GetPendingSessionRequests(jsonRpcHistory = get(), serializer = get()) }

    single<GetPendingAuthenticateRequestUseCaseInterface> { GetPendingAuthenticateRequestUseCase(jsonRpcHistory = get(), serializer = get()) }

    single { DeleteRequestByIdUseCase(jsonRpcHistory = get(), verifyContextStorageRepository = get()) }

    single { GetPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcHistory = get(), serializer = get()) }

    single { GetSessionRequestByIdUseCase(jsonRpcHistory = get(), serializer = get()) }

    single { GetPendingSessionAuthenticateRequest(jsonRpcHistory = get(), serializer = get()) }

    single { GetSessionAuthenticateRequest(jsonRpcHistory = get(), serializer = get()) }

    single { CacaoVerifier(projectId = get()) }

    single {
        SignEngine(
            verifyContextStorageRepository = get(),
            jsonRpcInteractor = get(),
            crypto = get(),
            authenticateResponseTopicRepository = get(),
            proposalStorageRepository = get(),
            authenticateSessionUseCase = get(),
            sessionStorageRepository = get(),
            metadataStorageRepository = get(),
            approveSessionUseCase = get(),
            disconnectSessionUseCase = get(),
            emitEventUseCase = get(),
            extendSessionUseCase = get(),
            decryptMessageUseCase = get(named(AndroidCommonDITags.DECRYPT_SIGN_MESSAGE)),
            getListOfVerifyContextsUseCase = get(),
            getPairingsUseCase = get(),
            getPendingRequestsByTopicUseCase = get(),
            getPendingSessionRequests = get(),
            getSessionProposalsUseCase = get(),
            getSessionsUseCase = get(),
            onPingUseCase = get(),
            getVerifyContextByIdUseCase = get(),
            onSessionDeleteUseCase = get(),
            onSessionEventUseCase = get(),
            onSessionExtendUseCase = get(),
            getPendingSessionRequestByTopicUseCase = get(),
            onSessionProposalResponseUseCase = get(),
            onSessionProposeUse = get(),
            onSessionRequestResponseUseCase = get(),
            onSessionRequestUseCase = get(),
            onSessionSettleResponseUseCase = get(),
            onSessionSettleUseCase = get(),
            onSessionUpdateResponseUseCase = get(),
            onSessionUpdateUseCase = get(),
            pairingController = get(),
            pairUseCase = get(),
            pingUseCase = get(),
            proposeSessionUseCase = get(),
            rejectSessionUseCase = get(),
            respondSessionRequestUseCase = get(),
            sessionRequestUseCase = get(),
            sessionUpdateUseCase = get(),
            onAuthenticateSessionUseCase = get(),
            onSessionAuthenticateResponseUseCase = get(),
            approveSessionAuthenticateUseCase = get(),
            rejectSessionAuthenticateUseCase = get(),
            formatAuthenticateMessageUseCase = get(),
            deleteRequestByIdUseCase = get(),
            getPendingAuthenticateRequestUseCase = get(),
            insertEventUseCase = get(),
            linkModeJsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }
}