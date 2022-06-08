package com.walletconnect.walletconnectv2.di

import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.walletconnectv2.network.Relay
import com.walletconnect.walletconnectv2.network.data.adapter.FlowStreamAdapter
import com.walletconnect.walletconnectv2.network.data.client.RelayClient
import com.walletconnect.walletconnectv2.network.data.connection.ConnectionType
import com.walletconnect.walletconnectv2.network.data.connection.controller.ConnectionController
import com.walletconnect.walletconnectv2.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.walletconnect.walletconnectv2.network.data.service.RelayService
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun networkModule(serverUrl: String, relay: Relay?, connectionType: ConnectionType) = module {
    val TIMEOUT_TIME = 5000L
    val DEFAULT_BACKOFF_MINUTES = 5L

    fun provideLifecycle(application: Application, connectionController: ConnectionController): Lifecycle {
        return if (connectionType == ConnectionType.MANUAL) {
            ManualConnectionLifecycle(connectionController, LifecycleRegistry())
        } else {
            AndroidLifecycle.ofApplicationForeground(application)
        }
    }

    fun provideLifecycleController(): ConnectionController {
        return if (connectionType == ConnectionType.MANUAL) {
            ConnectionController.Manual()
        } else {
            ConnectionController.Automatic
        }
    }

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
        provideLifecycleController()
    }

    single {
        provideLifecycle(application = androidApplication(), connectionController = get())
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

    single { get<Scarlet>().create(RelayService::class.java) }

    single { relay ?: RelayClient(get(), get()) }
}