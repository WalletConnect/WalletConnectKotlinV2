package com.walletconnect.web3.inbox.chat.request

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.chat.client.Chat
import com.walletconnect.web3.inbox.common.proxy.ChatProxyInteractor
import com.walletconnect.web3.inbox.json_rpc.Web3InboxParams
import com.walletconnect.web3.inbox.json_rpc.Web3InboxRPC

//todo note this is used in push as well.
internal abstract class ChatRequestUseCase<T : Web3InboxParams.Request>(val proxyInteractor: ChatProxyInteractor) {
    abstract operator fun invoke(rpc: Web3InboxRPC, params: T)

    //todo: discuss defining errors. Milestone 2
    fun respondWithError(rpc: Web3InboxRPC, error: Chat.Model.Error) =
        proxyInteractor.respond(JsonRpcResponse.JsonRpcError(id = rpc.id, error = JsonRpcResponse.Error(-1, error.toString())))

    fun respondWithResult(rpc: Web3InboxRPC, result: Any) =
        proxyInteractor.respond(JsonRpcResponse.JsonRpcResult(id = rpc.id, result = result))

    fun respondWithVoid(rpc: Web3InboxRPC) =
        proxyInteractor.respond(JsonRpcResponse.JsonRpcResult(id = rpc.id, result = emptyMap<Unit, Unit>())) // emptyMap because it is parsed as {}
}

