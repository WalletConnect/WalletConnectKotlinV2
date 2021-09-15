package org.walletconnect.walletconnectv2.relay

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.channels.BroadcastChannel
import okhttp3.OkHttpClient
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TtlAdapter
import org.walletconnect.walletconnectv2.relay.data.RelayService
import java.util.concurrent.TimeUnit

class DefaultRelayClient internal constructor(private val useTLs: Boolean, internal val hostName: String, internal val port: Int) {
    internal val okHttpClient = OkHttpClient.Builder()
        .writeTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .build()
    internal val webSocketFactory = okHttpClient.newWebSocketFactory(getServerUrl())
    internal val lifecycleRegistry = LifecycleRegistry()
    internal val moshi: Moshi = Moshi.Builder()
        .add(TopicAdapter)
        .add(ExpiryAdapter)
        .add(TtlAdapter)
        .add(JSONObjectAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()
    // TODO: Look into adding either a Flow adapter
    //  https://github.com/Tinder/Scarlet/issues/114#issuecomment-600256650 - sample Flow adapter
    //  https://github.com/kizok/tinder_scarlet_with_coroutine_adapter/blob/master/app/src/main/java/tech/kizok/sockettest/ScarletAdapter/ReceiveChannelStreamAdapter.kt - another flow adapter
    internal val scarlet = Scarlet.Builder()
        .webSocketFactory(webSocketFactory)
        .lifecycle(lifecycleRegistry)
        .addMessageAdapterFactory(MoshiMessageAdapter.Factory(moshi))
        .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
        .build()
    internal val relay: RelayService = scarlet.create()

    // job might be unnecessary since client has it's own coroutine context
//    private val job = SupervisorJob()
//    private val scope = CoroutineScope(job + Dispatchers.IO)

//    suspend fun wsConnect(block: suspend (DefaultClientWebSocketSession.() -> Unit) = {}) {
//        client.webSocket(urlString = getURL(), block = block)
//    }

    fun publish() {
        val test = object: Stream.Observer<WebSocket.Event> {
            override fun onComplete() {}

            override fun onError(throwable: Throwable) {}

            override fun onNext(data: WebSocket.Event) {}
        }
        relay.observeEvents().start(test)
    }

    private fun getServerUrl(): String = (if (useTLs) "wss" else "ws")+"://$hostName:$port"

    companion object {
        private const val defaultRemotePort = 443

        fun initRemote(useTLs: Boolean = false, hostName: String, port: Int = defaultRemotePort) =
            DefaultRelayClient(useTLs, hostName, port)
    }
}

