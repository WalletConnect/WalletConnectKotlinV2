@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient
import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.domain.KeyServerInteractor
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveIdentityUseCase
import com.walletconnect.chat.discovery.keyserver.domain.use_case.ResolveInviteUseCase
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

    single { KeyServerClient(get()) }


    single { RegisterIdentityUseCase(get()) }

    single { ResolveIdentityUseCase(get()) }

    single { RegisterInviteUseCase(get()) }

    single { ResolveInviteUseCase(get()) }

    single { KeyServerInteractor(get(named(ChatDITags.KEYSERVER_URL)), get(), get(), get(), get()) }
}