package com.walletconnect.android.internal.common.di

import android.net.Uri
import android.os.Build
import com.squareup.moshi.Moshi
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.walletconnect.android.BuildConfig
import com.walletconnect.android.internal.common.connection.ConnectivityState
import com.walletconnect.android.internal.common.connection.ManualConnectionLifecycle
import com.walletconnect.android.internal.common.jwt.clientid.GenerateJwtStoreClientIdUseCase
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.NetworkClientTimeout
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.data.adapter.FlowStreamAdapter
import com.walletconnect.foundation.network.data.service.RelayService
import com.walletconnect.utils.Empty
import com.walletconnect.utils.combineListOfBitSetsWithOrOperator
import com.walletconnect.utils.removeLeadingZeros
import com.walletconnect.utils.toBinaryString
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.IOException
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit

private const val FAIL_OVER_RELAY_URL: String = "wss://relay.walletconnect.org"
private const val DEFAULT_RELAY_URL: String = "relay.walletconnect.com"

@Suppress("LocalVariableName")
@JvmSynthetic
fun coreAndroidNetworkModule(serverUrl: String, connectionType: ConnectionType, sdkVersion: String, timeout: NetworkClientTimeout? = null) = module {
    val DEFAULT_BACKOFF_SECONDS = 5L
    val networkClientTimeout = timeout ?: NetworkClientTimeout.getDefaultTimeout()
    var SERVER_URL: String = serverUrl
    var wasFailOvered = false

    factory(named(AndroidCommonDITags.RELAY_URL)) {
        val jwt = get<GenerateJwtStoreClientIdUseCase>().invoke(SERVER_URL)
        Uri.parse(SERVER_URL)
            .buildUpon()
            .appendQueryParameter("auth", jwt)
            .appendQueryParameter("ua", get(named(AndroidCommonDITags.USER_AGENT)))
            .build()
            .toString()
    }

    factory(named(AndroidCommonDITags.USER_AGENT)) {
        val listOfSdkBitsets = getAll<BitSet>().takeUnless { it.isEmpty() } ?: listOf(BitSet())
        val sdkBitwiseFlags = if (listOfSdkBitsets.isNotEmpty()) {
            combineListOfBitSetsWithOrOperator(listOfSdkBitsets).toBinaryString().removeLeadingZeros()
        } else String.Empty
        """wc-2/kotlin-${sdkVersion}x$sdkBitwiseFlags/android-${Build.VERSION.RELEASE}"""
    }

    single {
        GenerateJwtStoreClientIdUseCase(get(), get())
    }

    single(named(AndroidCommonDITags.USER_AGENT_INTERCEPTOR)) {
        Interceptor { chain ->
            val updatedRequest = chain.request().newBuilder()
                .addHeader("User-Agent", get(named(AndroidCommonDITags.USER_AGENT)))
                .build()

            chain.proceed(updatedRequest)
        }
    }

    single<Interceptor?>(named(AndroidCommonDITags.LOGGING_INTERCEPTOR)) {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        } else {
            null
        }
    }

    single(named(AndroidCommonDITags.FAIL_OVER_INTERCEPTOR)) {
        Interceptor { chain ->
            val request = chain.request()
            try {
                if (wasFailOvered && request.url.host == DEFAULT_RELAY_URL) {
                    chain.proceed(request.newBuilder().url(get<String>(named(AndroidCommonDITags.RELAY_URL))).build())
                } else {
                    chain.proceed(request)
                }
            } catch (e: Exception) {
                if (request.url.host == DEFAULT_RELAY_URL && isFailOverException(e)) {
                    SERVER_URL = "$FAIL_OVER_RELAY_URL?projectId=${Uri.parse(SERVER_URL).getQueryParameter("projectId")}"
                    wasFailOvered = true
                    chain.proceed(request.newBuilder().url(get<String>(named(AndroidCommonDITags.RELAY_URL))).build())
                } else {
                    chain.proceed(request)
                }
            }
        }
    }

    single(named(AndroidCommonDITags.AUTHENTICATOR)) {
        Authenticator { _, response ->
            response.request.run {
                if (Uri.parse(SERVER_URL).host == this.url.host) {
                    this.newBuilder().url(get<String>(named(AndroidCommonDITags.RELAY_URL))).build()
                } else {
                    null
                }
            }
        }
    }

    single(named(AndroidCommonDITags.OK_HTTP)) {
        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>(named(AndroidCommonDITags.LOGGING_INTERCEPTOR)))
            .addInterceptor(get<Interceptor>(named(AndroidCommonDITags.USER_AGENT_INTERCEPTOR)))
            .addInterceptor(get<Interceptor>(named(AndroidCommonDITags.FAIL_OVER_INTERCEPTOR)))
            .authenticator((get(named(AndroidCommonDITags.AUTHENTICATOR))))
            .writeTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .readTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .callTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .connectTimeout(networkClientTimeout.timeout, networkClientTimeout.timeUnit)
            .build()
    }

    single(named(AndroidCommonDITags.MSG_ADAPTER)) { MoshiMessageAdapter.Factory(get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI)).build()) }

    single(named(AndroidCommonDITags.CONNECTION_CONTROLLER)) {
        if (connectionType == ConnectionType.MANUAL) {
            ConnectionController.Manual()
        } else {
            ConnectionController.Automatic
        }
    }

    single(named(AndroidCommonDITags.LIFECYCLE)) {
        if (connectionType == ConnectionType.MANUAL) {
            ManualConnectionLifecycle(get(named(AndroidCommonDITags.CONNECTION_CONTROLLER)), LifecycleRegistry())
        } else {
            AndroidLifecycle.ofApplicationForeground(androidApplication())
        }
    }

    single { LinearBackoffStrategy(TimeUnit.SECONDS.toMillis(DEFAULT_BACKOFF_SECONDS)) }

    single { FlowStreamAdapter.Factory() }

    single(named(AndroidCommonDITags.SCARLET)) {
        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>(named(AndroidCommonDITags.OK_HTTP)).newWebSocketFactory(get<String>(named(AndroidCommonDITags.RELAY_URL))))
            .lifecycle(get(named(AndroidCommonDITags.LIFECYCLE)))
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>(named(AndroidCommonDITags.MSG_ADAPTER)))
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single(named(AndroidCommonDITags.RELAY_SERVICE)) {
        get<Scarlet>(named(AndroidCommonDITags.SCARLET)).create(RelayService::class.java)
    }

    single(named(AndroidCommonDITags.CONNECTIVITY_STATE)) {
        ConnectivityState(androidApplication())
    }
}

private fun isFailOverException(e: Exception) = (e is SocketException || e is IOException)