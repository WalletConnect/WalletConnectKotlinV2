package com.walletconnect.auth.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.api.AndroidApiDITags
import com.walletconnect.android.impl.di.AndroidCoreDITags
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun commonModule() = module {

    includes(com.walletconnect.android.api.di.commonModule())

    single {
        get<Moshi>(named(AndroidApiDITags.MOSHI))
            .newBuilder()
            .build()
    }
}