package com.walletconnect.foundation.di

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.foundation.util.scope
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

fun networkModule(serverUrl: String, sdkVersion: String, jwt: String): Module = module {
    val DEFAULT_BACKOFF_SECONDS = 5L
    val TIMEOUT_TIME = 5000L

    // TODO: Setup env variable for tag instead of relayTest. Use env variable here instead of hard coded version
    single(named(FoundationDITags.INTERCEPTOR)) {
        Interceptor {
            val updatedRequest = it.request().newBuilder()
                .addHeader("User-Agent", "wc-2/kotlin-$sdkVersion-relayTest")
                .build()

            it.proceed(updatedRequest)
        }
    }

    single(named(FoundationDITags.OK_HTTP)) {
        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>(named(FoundationDITags.INTERCEPTOR)))
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single { MoshiMessageAdapter.Factory(get(named(FoundationDITags.MOSHI))) }

    single { FlowStreamAdapter.Factory() }

    single { LinearBackoffStrategy(TimeUnit.SECONDS.toMillis(DEFAULT_BACKOFF_SECONDS)) }

    single {
        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>(named(FoundationDITags.OK_HTTP)).newWebSocketFactory("$serverUrl&auth=$jwt"))
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }
    single { get<Scarlet>().create(RelayService::class.java) }

    single<RelayInterface> { object : BaseRelayClient(get(), get(), scope) {} }
}