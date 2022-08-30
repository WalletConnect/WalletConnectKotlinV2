package com.walletconnect.android.impl.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.impl.network.RelayConnectionInterface
import com.walletconnect.android.impl.network.data.connection.ConnectionType
import com.walletconnect.android.impl.network.data.connection.ConnectivityState
import com.walletconnect.android.impl.network.data.connection.controller.ConnectionController
import com.walletconnect.android.impl.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.walletconnect.android.impl.network.domain.RelayClient
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.walletconnect.foundation.di.networkModule as foundationNetworkModule

fun networkModule(serverUrl: String, jwt: String, connectionType: ConnectionType, sdkVersion: String, relay: RelayConnectionInterface?) = module {

    includes(foundationNetworkModule(serverUrl, sdkVersion, jwt))

    single(named(AndroidCoreDITags.INTERCEPTOR)) {
        Interceptor {
            val updatedRequest = it.request().newBuilder()
                .addHeader("User-Agent", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
                .build()

            it.proceed(updatedRequest)
        }
    }

    // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
    single(named(AndroidCoreDITags.OK_HTTP)) {
        get<OkHttpClient>(named(FoundationDITags.OK_HTTP)).newBuilder()
            .apply {
                interceptors().apply {
                    remove(get(named(FoundationDITags.INTERCEPTOR)))
                    add(get(named(AndroidCoreDITags.INTERCEPTOR)))
                }
            }
            .build()
    }

    single { MoshiMessageAdapter.Factory(get(named(AndroidCoreDITags.MOSHI))) }

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

    single { ConnectivityState(androidApplication()) }

    single {
        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>(named(AndroidCoreDITags.OK_HTTP)).newWebSocketFactory("$serverUrl&auth=$jwt"))
            .lifecycle(get())
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single { relay ?: RelayClient(get(), get(), get(), scope) }
}