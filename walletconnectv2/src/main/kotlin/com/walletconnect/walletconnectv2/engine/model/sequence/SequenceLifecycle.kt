package com.walletconnect.walletconnectv2.engine.model.sequence

import com.walletconnect.walletconnectv2.engine.model.EngineModel

internal sealed class SequenceLifecycle {
    class OnSessionProposal(val proposal: EngineModel.SessionProposalDO) : SequenceLifecycle()
    class OnSessionRequest(val request: EngineModel.SessionRequestDO) : SequenceLifecycle()
    class OnSessionDeleted(val deletedSession: EngineModel.DeletedSessionDO) : SequenceLifecycle()
    class OnSessionNotification(val notification: EngineModel.SessionNotificationDO) : SequenceLifecycle()
    object Default : SequenceLifecycle()
}