package org.walletconnect.walletconnectv2.jsonrpc

import org.walletconnect.walletconnectv2.clientsync.ClientSyncJsonRpc
import org.walletconnect.walletconnectv2.common.Topic

interface JsonRpcSerializing {
    fun serialize(payload: ClientSyncJsonRpc, topic: Topic): String
    fun decode(message: String, topic: Topic): String
}