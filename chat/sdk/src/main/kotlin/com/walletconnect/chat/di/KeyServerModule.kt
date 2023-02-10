@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.domain.use_case.*
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.UnregisterIdentityUseCase
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun keyServerModule(keyServerUrl: String) = module {
    val TIMEOUT_TIME = 5000L

    single {
        // TODO: Should be extracted to core module
        OkHttpClient.Builder()
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single(named(ChatDITags.KEYSERVER_URL)) { keyServerUrl }

    single {
        Retrofit.Builder()
            .baseUrl(keyServerUrl)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single {
        get<Retrofit>().create(KeyServerService::class.java)
    }

    single { RegisterIdentityUseCase(get()) }

    single { UnregisterIdentityUseCase(get()) }

    single { ResolveIdentityUseCase(get()) }

    single { RegisterInviteUseCase(get()) }

    single { UnregisterInviteUseCase(get()) }

    single { ResolveInviteUseCase(get()) }
}