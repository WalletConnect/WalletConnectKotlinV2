package com.walletconnect.sign.di

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.sign.network.Relay
import com.walletconnect.sign.network.data.adapter.FlowStreamAdapter
import com.walletconnect.sign.network.data.client.RelayClient
import com.walletconnect.sign.network.data.connection.ConnectionType
import com.walletconnect.sign.network.data.connection.controller.ConnectionController
import com.walletconnect.sign.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.walletconnect.sign.network.data.service.RelayService
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun scarletModule(serverUrl: String, jwt: String, connectionType: ConnectionType, relay: Relay?) = module {
    val DEFAULT_BACKOFF_MINUTES = 5L

    single { MoshiMessageAdapter.Factory(get()) }

    single { FlowStreamAdapter.Factory() }

    single { LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)) }

    single {
        if (connectionType == ConnectionType.MANUAL) {
            ConnectionController.Manual()
        } else {
            ConnectionController.Automatic
        }
    }

    single {
        if (connectionType == ConnectionType.MANUAL) {
            ManualConnectionLifecycle(get(), LifecycleRegistry())
        } else {
            AndroidLifecycle.ofApplicationForeground(androidApplication())
        }
    }

    single {
        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>().newWebSocketFactory("$serverUrl&auth=$jwt"))
            .lifecycle(get())
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }


    single { get<Scarlet>().create(RelayService::class.java) }

    single { relay ?: RelayClient(get(), get()) }
}