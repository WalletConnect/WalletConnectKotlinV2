package com.walletconnect.android.internal.common.di

import com.walletconnect.android.push.network.PushService
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun pushModule() = module {

    factory(named(AndroidCommonDITags.PUSH_URL)) { "https://echo.walletconnect.org/" }

    single(named(AndroidCommonDITags.PUSH_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(get<String>(named(AndroidCommonDITags.PUSH_URL)))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .build()
    }

    single {
        get<Retrofit>(named(AndroidCommonDITags.PUSH_RETROFIT)).create(PushService::class.java)
    }

    single<MutableMap<String, DecryptMessageUseCaseInterface>>(named(AndroidCommonDITags.DECRYPT_USE_CASES)) { mutableMapOf() }
}