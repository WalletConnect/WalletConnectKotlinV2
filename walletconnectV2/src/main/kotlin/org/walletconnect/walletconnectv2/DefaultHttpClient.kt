package org.walletconnect.walletconnectv2

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.Closeable

class DefaultHttpClient private constructor(private val useTLs: Boolean, internal val hostName: String, internal val port: Int): Closeable {
    internal var client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    // job might be unnecessary since client has it's own coroutine context
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    suspend fun wsConnect(block: suspend (DefaultClientWebSocketSession.() -> Unit) = {}) {
        client.webSocket(urlString = getURL(), block = block)
    }

    private fun getURL(): String = if (useTLs) "wss://$hostName:$port" else "ws://$hostName:$port"

    override fun close() = client.close()

    companion object {
        internal const val defaultLocalPort = 1025
        private const val defaultRemotePort = 443

        fun initLocal(useTLs: Boolean = false, port: Int = defaultLocalPort) =
            DefaultHttpClient(useTLs, "127.0.0.1", port)

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = defaultRemotePort) =
            DefaultHttpClient(useTLs, hostName, port)
    }
}

