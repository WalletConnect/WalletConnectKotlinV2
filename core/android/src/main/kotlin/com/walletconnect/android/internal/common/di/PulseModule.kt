package com.walletconnect.android.internal.common.di

import com.walletconnect.android.keyserver.data.service.KeyServerService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
fun pulseModule() = module {
//    single(named(AndroidCommonDITags.KEYSERVER_URL)) { keyServerUrl }

    single(named(AndroidCommonDITags.KEYSERVER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl("")
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.KEYSERVER_RETROFIT)).create(KeyServerService::class.java) }
}