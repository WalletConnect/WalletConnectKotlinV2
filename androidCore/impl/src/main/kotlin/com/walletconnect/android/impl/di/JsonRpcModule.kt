package com.walletconnect.android.impl.di

import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.android.internal.common.SerializableJsonRpc
import com.walletconnect.android.internal.common.model.JsonRpcInteractorInterface
import com.walletconnect.android.pairing.PairingJsonRpcMethod
import com.walletconnect.android.pairing.PairingParams
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module
import kotlin.reflect.KClass

@JvmSynthetic
fun jsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(get(), get(), get())
    }

    addSerializerEntry(PairingParams.PingParams::class)
    addSerializerEntry(PairingParams.DeleteParams::class)

    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_PING, PairingParams.PingParams::class)
    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingParams.DeleteParams::class)

    factory {
        JsonRpcSerializer(
            serializerEntries = getAll<KClass<SerializableJsonRpc>>().toSet(),
            deserializerEntries = getAll<Pair<String, KClass<*>>>().toMap()
        )
    }
}