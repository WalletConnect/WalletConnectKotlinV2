package com.walletconnect.auth.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.api.AndroidApiDITags
import com.walletconnect.android.api.JsonRpcResponse
import com.walletconnect.auth.common.adapters.JsonRpcResultAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName

@JvmSynthetic
internal fun commonModule() = module {

    includes(com.walletconnect.android.api.di.commonModule())

    single {
        get<Moshi>(named(AndroidApiDITags.MOSHI))
            .newBuilder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .build()
    }
}