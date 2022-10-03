package com.walletconnect.auth.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.common.di.AndroidCommonDITags
import com.walletconnect.auth.common.adapters.JsonRpcResultAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName
import com.walletconnect.android.common.di.commonModule as androidCommonModule

@JvmSynthetic
internal fun commonModule() = module {

    includes(androidCommonModule())

    //todo: Maybe this needs to be registered as named(AndroidCommonDITags.MOSHI) to properly work with new JsonRpcSerializer approach
    single {
        get<Moshi>(named(AndroidCommonDITags.MOSHI))
            .newBuilder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    com.walletconnect.android.common.JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .build()
    }
}