@file:JvmSynthetic

package com.walletconnect.sign.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android_core.di.AndroidCoreDITags
import com.walletconnect.android_core.json_rpc.model.JsonRpcResponse
import com.walletconnect.sign.common.adapters.JsonRpcResultAdapter
import com.walletconnect.sign.common.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName
import com.walletconnect.android_core.di.commonModule as androidCoreCommonModule

@JvmSynthetic
internal fun commonModule() = module {

    includes(androidCoreCommonModule())

    single {
        get<Moshi>(named(AndroidCoreDITags.MOSHI))
            .newBuilder()
            .add { type, _, moshi ->
                when (type.getRawType().name) {
                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                    JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .build()
    }
}