@file:JvmSynthetic

package com.walletconnect.sign.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android.api.AndroidApiDITags
import com.walletconnect.android.impl.di.AndroidCoreDITags
import com.walletconnect.android.api.JsonRpc
import com.walletconnect.sign.common.adapters.JsonRpcResultAdapter
import com.walletconnect.sign.common.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionRequestVO
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
                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                    JsonRpc.JsonRpcResponse.JsonRpcResult::class.jvmName -> JsonRpcResultAdapter(moshi)
                    else -> null
                }
            }
            .build()
    }
}