package org.walletconnect.walletconnectv2.engine.sequence

import org.walletconnect.walletconnectv2.engine.model.EngineData

sealed class SequenceLifecycleEvent {
    class OnSessionProposal(val proposal: EngineData.SessionProposal) : SequenceLifecycleEvent()
    class OnSessionRequest(val request: EngineData.SessionRequest) : SequenceLifecycleEvent()
    class OnSessionSettled(val session: EngineData.SettledSession) : SequenceLifecycleEvent()
    class OnSessionDeleted(val topic: String, val reason: String) : SequenceLifecycleEvent()
    object Unsupported : SequenceLifecycleEvent()
    object Default : SequenceLifecycleEvent()
}