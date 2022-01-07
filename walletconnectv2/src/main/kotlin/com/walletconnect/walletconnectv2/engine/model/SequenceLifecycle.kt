package com.walletconnect.walletconnectv2.engine.model

internal sealed class SequenceLifecycle {
    class OnSessionProposal(val proposal: EngineModel.SessionProposalDO) : SequenceLifecycle()
    class OnSessionRequest(val request: EngineModel.SessionRequest) : SequenceLifecycle()
    class OnSessionDeleted(val deletedSession: EngineModel.DeletedSession) : SequenceLifecycle()
    class OnSessionNotification(val notification: EngineModel.SessionNotification) : SequenceLifecycle()
    object Default : SequenceLifecycle()
}