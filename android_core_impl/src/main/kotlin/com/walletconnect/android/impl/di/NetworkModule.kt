package com.walletconnect.android.impl.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.android.api.*
import com.walletconnect.android.api.di.androidApiNetworkModule
import com.walletconnect.android.impl.network.data.connection.ConnectivityState
import com.walletconnect.foundation.di.FoundationDITags
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.network.data.service.RelayService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.walletconnect.foundation.di.networkModule as foundationNetworkModule

//todo: make user to pass connection type once; Init params or RelayClient?
fun networkModule(relay: RelayConnectionInterface) = module {

    //todo: should pass params from relay and include androidApiModule?
//    includes(androidApiNetworkModule(serverUrl, jwt, connectionType, sdkVersion))
//    includes(foundationNetworkModule(serverUrl, sdkVersion, jwt))

    single { ConnectivityState(androidApplication()) }

    single<RelayConnectionInterface>() {
        println("kobe; Sign RelayClient")
        relay
    } //?: RelayClient(get(), get(), get(), scope) }


//
//    single(named(AndroidCoreDITags.INTERCEPTOR)) {
//        Interceptor { chain ->
//            val updatedRequest = chain.request().newBuilder()
//                .addHeader("User-Agent", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
//                .build()
//
//            chain.proceed(updatedRequest)
//        }
//    }
//
//    // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
//    single(named(AndroidCoreDITags.OK_HTTP)) {
//        get<OkHttpClient>(named(FoundationDITags.OK_HTTP)).newBuilder()
//            .apply {
//                interceptors().apply {
//                    remove(get(named(FoundationDITags.INTERCEPTOR)))
//                    add(get(named(AndroidCoreDITags.INTERCEPTOR)))
//                }
//            }
//            .build()
//    }
//
//    single { MoshiMessageAdapter.Factory(get(named(AndroidCoreDITags.MOSHI))) }

//    single {
//        if (connectionType == ConnectionType.MANUAL) {
//            ConnectionController.Manual()
//        } else {
//            ConnectionController.Automatic
//        }
//    }

//    single {
//        if (connectionType == ConnectionType.MANUAL) {
//            ManualConnectionLifecycle(get(), LifecycleRegistry())
//        } else {
//            AndroidLifecycle.ofApplicationForeground(androidApplication())
//        }
//    }

//    single<RelayService> { get<Scarlet>().create(RelayService::class.java) }
////
//    single {
//        Scarlet.Builder()
//            .backoffStrategy(get<LinearBackoffStrategy>())
//            .webSocketFactory(get<OkHttpClient>(named(AndroidCoreDITags.OK_HTTP)).newWebSocketFactory("$serverUrl&auth=$jwt"))
//            .lifecycle(get())
//            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
//            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
//            .build()
//    }
//
}