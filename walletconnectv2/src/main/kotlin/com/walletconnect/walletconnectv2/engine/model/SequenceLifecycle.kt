package com.walletconnect.walletconnectv2.engine.model

internal sealed class SequenceLifecycle {
    class OnSessionProposal(val proposal: EngineData.SessionProposalDO) : SequenceLifecycle()
    class OnSessionRequest(val request: EngineData.SessionRequest) : SequenceLifecycle()
    class OnSessionDeleted(val deletedSession: EngineData.DeletedSession) : SequenceLifecycle()
    class OnSessionNotification(val notification: EngineData.SessionNotification) : SequenceLifecycle()
    object Default : SequenceLifecycle()
}