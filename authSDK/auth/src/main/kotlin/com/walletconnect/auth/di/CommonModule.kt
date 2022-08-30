package com.walletconnect.auth.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.impl.di.AndroidCoreDITags
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.walletconnect.android.impl.di.commonModule as androidCoreCommonModule

@JvmSynthetic
internal fun commonModule() = module {

    includes(androidCoreCommonModule())

    single {
        get<Moshi>(named(AndroidCoreDITags.MOSHI))
            .newBuilder()
            .build()
    }
}