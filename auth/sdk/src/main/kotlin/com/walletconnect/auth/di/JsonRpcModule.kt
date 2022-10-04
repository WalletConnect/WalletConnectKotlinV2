package com.walletconnect.auth.di

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.auth.common.json_rpc.AuthRpc
import com.walletconnect.auth.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.intoMultibindingMap
import com.walletconnect.utils.intoMultibindingSet
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule

@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    intoMultibindingSet { payload: SerializableJsonRpc ->
        payload is AuthRpc.AuthRequest
    }

    intoMultibindingMap(JsonRpcMethod.WC_AUTH_REQUEST, AuthRpc.AuthRequest::class)
}