package com.walletconect.android_core.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconect.android_core.network.RelayConnectionInterface
import com.walletconect.android_core.network.data.connection.ConnectionType
import com.walletconect.android_core.network.data.connection.controller.ConnectionController
import com.walletconect.android_core.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.walletconect.android_core.network.domain.RelayClient
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun networkModule(serverUrl: String, jwt: String, connectionType: ConnectionType, relay: RelayConnectionInterface?) = module {
    val DEFAULT_BACKOFF_SECONDS = 5L
    val TIMEOUT_TIME = 5000L

    // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                val updatedRequest = it.request().newBuilder()
                    .addHeader("User-Agent", """wc-2/kotlin-2.0.0-rc.0/android-${Build.VERSION.RELEASE}""")
                    .build()

                it.proceed(updatedRequest)
            }
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single { MoshiMessageAdapter.Factory(get()) }

    single { FlowStreamAdapter.Factory() }

    single { LinearBackoffStrategy(TimeUnit.SECONDS.toMillis(DEFAULT_BACKOFF_SECONDS)) }

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

    single { relay ?: RelayClient(get(), get(), get(), get()) }
}