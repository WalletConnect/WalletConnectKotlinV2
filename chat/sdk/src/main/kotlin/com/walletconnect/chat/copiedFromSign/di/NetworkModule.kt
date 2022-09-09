package com.walletconnect.chat.copiedFromSign.di

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.chat.copiedFromSign.network.RelayInterface
import com.walletconnect.chat.copiedFromSign.network.adapter.FlowStreamAdapter
import com.walletconnect.chat.copiedFromSign.network.connection.ConnectionType
import com.walletconnect.chat.copiedFromSign.network.connection.controller.ConnectionController
import com.walletconnect.chat.copiedFromSign.network.connection.lifecycle.ManualConnectionLifecycle
import com.walletconnect.chat.copiedFromSign.network.domain.RelayClient
import com.walletconnect.chat.copiedFromSign.network.service.RelayService
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun networkModule(serverUrl: String, connectionType: ConnectionType = ConnectionType.AUTOMATIC) = module {
    val TIMEOUT_TIME = 5000L
    val DEFAULT_BACKOFF_MINUTES = 5L

    single { MoshiMessageAdapter.Factory(get()) }

    single { FlowStreamAdapter.Factory() }

    single {
        OkHttpClient.Builder()
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

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
            .backoffStrategy(LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)))
            .webSocketFactory(get<OkHttpClient>().newWebSocketFactory(serverUrl))
            .lifecycle(get())
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single {
        get<Scarlet>().create(RelayService::class.java)
    }

//    single { relay ?: RelayClient(get(), get()) }
//    TODO: Figure out how to get relay as in Sign in here
    single<RelayInterface> { RelayClient(get(), get()) }
}