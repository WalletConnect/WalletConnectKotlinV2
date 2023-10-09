@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.json_rpc.domain.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.sign.json_rpc.domain.GetPendingSessionRequests
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    includes(callsModule(), requestsModule(), responsesModule())

    single { GetPendingSessionRequests(jsonRpcHistory = get(), serializer = get()) }

    single { GetPendingJsonRpcHistoryEntryByIdUseCase(jsonRpcHistory = get(), serializer = get()) }

    single {
        SignEngine(
            verifyContextStorageRepository = get(),
            jsonRpcInteractor = get(),
            crypto = get(),
            proposalStorageRepository = get(),
            sessionStorageRepository = get(),
            metadataStorageRepository = get(),
            approveSessionUseCase = get(),
            disconnectSessionUseCase = get(),
            emitEventUseCase = get(),
            extendSessionUseCase = get(),
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
        )
    }
}