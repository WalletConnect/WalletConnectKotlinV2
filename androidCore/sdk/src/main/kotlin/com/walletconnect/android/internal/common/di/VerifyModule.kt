package com.walletconnect.android.internal.common.di

import com.walletconnect.android.verify.data.VerifyService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun verifyModule(verifyServerUrl: String?) = module {
    val verifyUrl: String = verifyServerUrl ?: VERIFY_SERVER_URL

    single(named(AndroidCommonDITags.VERIFY_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(verifyUrl)
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single(named("VerifyService")) {
        println("kobe; VerifyService")
        get<Retrofit>(named(AndroidCommonDITags.VERIFY_RETROFIT)).create(VerifyService::class.java)
    }
}

private const val VERIFY_SERVER_URL = "https://verify.walletconnect.com/"