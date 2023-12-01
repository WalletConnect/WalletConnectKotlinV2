package com.walletconnect.android.internal.common.di

import android.content.SharedPreferences
import com.walletconnect.android.push.PushInterface
import com.walletconnect.android.push.network.PushService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun pushModule() = module {

    factory(named(AndroidCommonDITags.PUSH_URL)) { PUSH_URL }

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

    single(named(AndroidCommonDITags.CLIENT_ID)) {
        requireNotNull(get<SharedPreferences>().getString(PushInterface.KEY_CLIENT_ID, null))
    }
}