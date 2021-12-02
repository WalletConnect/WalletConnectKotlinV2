package org.walletconnect.walletconnectv2.engine.sequence

import org.walletconnect.walletconnectv2.engine.model.EngineData

internal sealed class SequenceLifecycle {
    class OnSessionProposal(val proposal: EngineData.SessionProposal) : SequenceLifecycle()
    class OnSessionRequest(val request: EngineData.SessionRequest) : SequenceLifecycle()
    class OnSessionDeleted(val deletedSession: EngineData.DeletedSession) : SequenceLifecycle()
    class OnSessionNotification(val notification: EngineData.SessionNotification) : SequenceLifecycle()
    object Default : SequenceLifecycle()
}