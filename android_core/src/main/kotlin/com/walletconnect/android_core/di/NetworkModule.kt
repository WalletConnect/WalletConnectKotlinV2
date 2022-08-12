package com.walletconnect.android_core.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.android_core.network.data.connection.ConnectivityState
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectionType
import com.walletconnect.android_core.network.data.connection.controller.ConnectionController
import com.walletconnect.android_core.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.walletconnect.android_core.network.domain.RelayClient
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.di.networkModule as foundationNetworkModule
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

fun networkModule(serverUrl: String, jwt: String, connectionType: ConnectionType, sdkVersion: String, relay: RelayConnectionInterface?) = module {
    val TIMEOUT_TIME = 5000L

    includes(foundationNetworkModule(serverUrl, sdkVersion, jwt))

    // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                val updatedRequest = it.request().newBuilder()
                    .addHeader("User-Agent", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
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

    single { ConnectivityState(get()) }
}