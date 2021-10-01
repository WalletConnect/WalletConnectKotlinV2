package org.walletconnect.walletconnectv2.util

import org.walletconnect.walletconnectv2.relay.DefaultRelayClient

private const val STRING_LENGTH = 64
private val CHAR_POOL: List<Char> = ('A'..'F') + ('0'..'9')

internal fun getRandom64ByteHexString() =
    (1..STRING_LENGTH)
        .map { CHAR_POOL.random() }
        .joinToString("")

internal const val defaultLocalPort = 1025
internal fun initLocal(useTLs: Boolean = false, port: Int = defaultLocalPort) =
    DefaultRelayClient(useTLs, "127.0.0.1", port)