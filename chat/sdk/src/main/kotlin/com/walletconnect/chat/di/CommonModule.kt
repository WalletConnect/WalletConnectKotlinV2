@file:JvmSynthetic

package com.walletconnect.chat.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.common.JsonRpcResponse
import com.walletconnect.android.common.di.AndroidCommonDITags
import com.walletconnect.chat.common.adapter.JsonRpcResultAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName

@JvmSynthetic
internal fun commonModule() = module {

    includes(com.walletconnect.android.common.di.commonModule())

    single {
        get<Moshi>(named(AndroidCommonDITags.MOSHI))
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