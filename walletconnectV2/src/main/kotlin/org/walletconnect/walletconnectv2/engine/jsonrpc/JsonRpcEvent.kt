package org.walletconnect.walletconnectv2.engine.jsonrpc

import org.walletconnect.walletconnectv2.engine.model.EngineData


sealed class JsonRpcEvent {

    class OnSessionProposal(val proposal: EngineData.SessionProposal) : JsonRpcEvent()

    class OnSessionRequest(val payload: Any) : JsonRpcEvent()

    object Unsupported : JsonRpcEvent()

    object Default : JsonRpcEvent()
}