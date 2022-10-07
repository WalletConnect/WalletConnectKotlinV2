package com.walletconnect.android.impl.di

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.android.common.model.JsonRpcInteractorInterface
import com.walletconnect.android.impl.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.impl.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.android.internal.PairingJsonRpcMethod
import com.walletconnect.android.internal.PairingParams
import com.walletconnect.utils.intoMultibindingMap
import com.walletconnect.utils.intoMultibindingSet
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.KClass

@JvmSynthetic
fun jsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(get(), get(), get())
    }

    single(named(AndroidCoreDITags.SERIALIZER_SET), createdAtStart = true) {
        mutableSetOf<(SerializableJsonRpc) -> Boolean>()
    }

    single(named(AndroidCoreDITags.DESERIALIZER_MAP), createdAtStart = true) {
        mutableMapOf<String, KClass<*>>()
    }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is PairingParams.SessionProposeParams }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is PairingParams.PingParams }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is PairingParams.DeleteParams }

    intoMultibindingMap(PairingJsonRpcMethod.WC_SESSION_PROPOSE, PairingParams.SessionProposeParams::class)

    intoMultibindingMap(PairingJsonRpcMethod.WC_PAIRING_PING, PairingParams.PingParams::class)

    intoMultibindingMap(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingParams.DeleteParams::class)

    factory {
        JsonRpcSerializer(
            serializerEntries = get(named(AndroidCoreDITags.SERIALIZER_SET)),
            deserializerEntries = get(named(AndroidCoreDITags.DESERIALIZER_MAP))
        )
    }
}