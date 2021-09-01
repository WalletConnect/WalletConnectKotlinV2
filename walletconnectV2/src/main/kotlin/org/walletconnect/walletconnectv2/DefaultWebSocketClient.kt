package org.walletconnect.walletconnectv2

import com.squareup.moshi.Moshi
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import okhttp3.OkHttpClient
import org.walletconnect.walletconnectv2.data.RelayService
import java.util.concurrent.TimeUnit

class DefaultWebSocketClient private constructor(private val useTLs: Boolean, internal val hostName: String, internal val port: Int) {
    internal val okHttpClient = OkHttpClient.Builder()
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    internal val webSocketFactory = okHttpClient.newWebSocketFactory(getServerUrl())
    internal val lifecycleRegistry = LifecycleRegistry()
    internal val moshi: Moshi = Moshi.Builder().build()
    internal val scarlet = Scarlet.Builder()
        .webSocketFactory(webSocketFactory)
        .lifecycle(lifecycleRegistry)
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
        .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        .build()
    internal val relay: RelayService = scarlet.create()

    // job might be unnecessary since client has it's own coroutine context
//    private val job = SupervisorJob()
//    private val scope = CoroutineScope(job + Dispatchers.IO)

//    suspend fun wsConnect(block: suspend (DefaultClientWebSocketSession.() -> Unit) = {}) {
//        client.webSocket(urlString = getURL(), block = block)
//    }

    fun test() {
        relay.send("".toByteArray())
    }

    private fun getServerUrl(): String = if (useTLs) "wss://$hostName:$port" else "ws://$hostName:$port"

    companion object {
        internal const val defaultLocalPort = 1025
        private const val defaultRemotePort = 443

        internal fun initLocal(useTLs: Boolean = false, port: Int = defaultLocalPort) =
            DefaultWebSocketClient(useTLs, "127.0.0.1", port)

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = defaultRemotePort) =
            DefaultWebSocketClient(useTLs, hostName, port)
    }
}

