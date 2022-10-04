package com.walletconnect.android.impl.di

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.JsonRpcInteractorInterface
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.domain.JsonRpcInteractor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.KClass

@JvmSynthetic
fun jsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(get(), get(), get())
    }

    single(named(AndroidCoreDITags.SERIALIZER_SET)) {
        mutableSetOf<(SerializableJsonRpc) -> Boolean>()
    }

    single(named(AndroidCoreDITags.DESERIALIZER_MAP), createdAtStart = true) {
        mutableMapOf<String, KClass<*>>()
    }
    
    factory { JsonRpcSerializer(get(), serializerEntries = get(named(AndroidCoreDITags.SERIALIZER_SET)), deserializerEntries = get(named(AndroidCoreDITags.DESERIALIZER_MAP))) }
}