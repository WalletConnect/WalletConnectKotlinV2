package com.walletconnect.sign.di

import com.squareup.moshi.Moshi
import com.tinder.scarlet.utils.getRawType
import com.walletconnect.android_core.json_rpc.model.JsonRpc
import com.walletconnect.sign.core.adapters.RelayDOJsonRpcResultJsonAdapter
import com.walletconnect.sign.core.adapters.SessionRequestVOJsonAdapter
import com.walletconnect.sign.core.model.vo.clientsync.session.payload.SessionRequestVO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName
import com.walletconnect.android_core.di.commonModule as androidCoreCommonModule

fun commonModule() = module {

    includes(androidCoreCommonModule())

    single {
        get<Moshi>(named("foundation"))
            .newBuilder()
            .addLast { type, _, moshi ->
                when (type.getRawType().name) {
                    SessionRequestVO::class.jvmName -> SessionRequestVOJsonAdapter(moshi)
                    JsonRpc.JsonRpcResponse.JsonRpcResult::class.jvmName -> RelayDOJsonRpcResultJsonAdapter(moshi)
                    else -> null
                }
            }
            .build()
    }
}