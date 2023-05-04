@file:JvmSynthetic

package com.walletconnect.android.internal.common.di

import com.walletconnect.android.internal.common.explorer.ExplorerRepository
import com.walletconnect.android.internal.common.explorer.data.network.ExplorerService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@JvmSynthetic
internal fun explorerModule() = module {

    single(named(AndroidCommonDITags.EXPLORER_URL)) { "https://registry.walletconnect.com/v3/" }

    single(named(AndroidCommonDITags.EXPLORER_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(get<String>(named(AndroidCommonDITags.EXPLORER_URL)))
            .client(get(named(AndroidCommonDITags.OK_HTTP)))
            .addConverterFactory(MoshiConverterFactory.create(get(named(AndroidCommonDITags.MOSHI))))
            .build()
    }

    single { get<Retrofit>(named(AndroidCommonDITags.EXPLORER_RETROFIT)).create(ExplorerService::class.java) }

    single { ExplorerRepository(get()) }
}