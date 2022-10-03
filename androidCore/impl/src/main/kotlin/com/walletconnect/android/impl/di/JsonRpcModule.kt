package com.walletconnect.android.impl.di

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.ClientParams
import com.walletconnect.android.common.model.JsonRpcInteractorInterface
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.domain.JsonRpcInteractor
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
fun jsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(get(), get(), get())
    }

    single(named("serializerMap")) {
        mutableListOf<Pair<(SerializableJsonRpc) -> Boolean, (SerializableJsonRpc) -> String>>()
    }

    single(named("deserializerMap")) {
        mutableListOf<Pair<(String) -> Boolean, (String) -> ClientParams?>>()
    }
    
    factory { JsonRpcSerializer(get(), serializerEntries = get(named("serializerMap")), deserializerEntries = get(named("deserializerMap"))) }
}