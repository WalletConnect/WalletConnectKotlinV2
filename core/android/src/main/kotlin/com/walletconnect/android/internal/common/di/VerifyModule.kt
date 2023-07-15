package com.walletconnect.android.internal.common.di

import com.walletconnect.android.verify.data.VerifyService
import com.walletconnect.android.verify.domain.ResolveAttestationIdUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun verifyModule(verifyServerUrl: String?) = module {
    val verifyUrl: String = verifyServerUrl ?: VERIFY_SERVER_URL

    single(named(AndroidCommonDITags.VERIFY_URL)) { verifyUrl }

    single(named(AndroidCommonDITags.VERIFY_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(verifyUrl)
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single {
        get<Retrofit>(named(AndroidCommonDITags.VERIFY_RETROFIT)).create(VerifyService::class.java)
    }

    single { ResolveAttestationIdUseCase(get(), get(), get(named(AndroidCommonDITags.VERIFY_URL))) }
}

private const val VERIFY_SERVER_URL = "https://verify.walletconnect.com/"