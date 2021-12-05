package org.walletconnect.walletconnectv2.jsonrpc

import org.walletconnect.walletconnectv2.common.Topic

interface JsonRpcConverting {
    fun encode(json: String, topic: Topic): String
    fun decode(message: String, topic: Topic): String
}