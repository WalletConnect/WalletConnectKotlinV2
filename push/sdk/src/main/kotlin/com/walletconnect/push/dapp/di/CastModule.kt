package com.walletconnect.push.dapp.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.push.PushDatabase
import com.walletconnect.push.dapp.data.CastRepository
import com.walletconnect.push.dapp.data.network.CastService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@JvmSynthetic
internal fun castModule(castServerUrl: String? = null) = module {

    single(named(PushDITags.CAST_SERVER_URL)) {
        castServerUrl ?: "https://cast.walletconnect.com/"
    }

    single(named(PushDITags.CAST_RETROFIT)) {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

        Retrofit.Builder()
            .baseUrl(get<String>(named(PushDITags.CAST_SERVER_URL)))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .build()
    }

    single {
        get<Retrofit>(named(PushDITags.CAST_RETROFIT)).create(CastService::class.java)
    }

    single {
        get<PushDatabase>().pendingRegisterRequestsQueries
    }

    single {
        CastRepository(get(), get(), get())
    }
}