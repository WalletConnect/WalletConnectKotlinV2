package org.walletconnect.walletconnectv2.engine.jsonrpc

import org.walletconnect.walletconnectv2.client.SessionProposal

sealed class JsonRpcEvent
class OnSessionProposal(val proposal: SessionProposal) : JsonRpcEvent()
class OnSessionRequest(val payload: Any) : JsonRpcEvent()
object Unsupported : JsonRpcEvent()
object Default : JsonRpcEvent()