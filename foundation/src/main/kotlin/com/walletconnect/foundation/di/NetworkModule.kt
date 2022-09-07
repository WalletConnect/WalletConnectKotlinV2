package com.walletconnect.foundation.di

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.RelayInterface
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.network.data.service.RelayService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single<MoshiMessageAdapter.Factory>(named(FoundationDITags.MSG_ADAPTER)) { MoshiMessageAdapter.Factory(get(named(FoundationDITags.MOSHI))) }

    single { FlowStreamAdapter.Factory() }

    single { LinearBackoffStrategy(TimeUnit.SECONDS.toMillis(DEFAULT_BACKOFF_SECONDS)) }

    single<ConnectionController>() {

        ConnectionController.Manual()
    }

    single(named(FoundationDITags.SCARLET)) {

        println("kobe; Base Scarlet: $serverUrl&auth=$jwt")

        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>(named(FoundationDITags.OK_HTTP)).newWebSocketFactory("$serverUrl&auth=$jwt"))
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>(named(FoundationDITags.MSG_ADAPTER)))
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single<RelayService>(named(FoundationDITags.RELAY_SERVICE)) { get<Scarlet>(named(FoundationDITags.SCARLET)).create(RelayService::class.java) }

    single<RelayInterface> { object : BaseRelayClient() {} }
}