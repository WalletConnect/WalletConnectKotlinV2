package com.walletconnect.android.internal.common.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.domain.JsonRpcInteractor
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.type.SerializableJsonRpc
import com.walletconnect.android.pairing.model.PairingJsonRpcMethod
import com.walletconnect.android.pairing.model.PairingRpc
import com.walletconnect.utils.JsonAdapterEntry
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.KClass

@JvmSynthetic
fun coreJsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(
            relay = get(),
            chaChaPolyCodec = get(),
            jsonRpcHistory = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            historyMessageNotifier = get()
        )
    }

    addSerializerEntry(PairingRpc.PairingPing::class)
    addSerializerEntry(PairingRpc.PairingDelete::class)

    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_PING, PairingRpc.PairingPing::class)
    addDeserializerEntry(PairingJsonRpcMethod.WC_PAIRING_DELETE, PairingRpc.PairingDelete::class)

    factory {
        JsonRpcSerializer(
            serializerEntries = getAll<KClass<SerializableJsonRpc>>().toSet(),
            deserializerEntries = getAll<Pair<String, KClass<*>>>().toMap(),
            jsonAdapterEntries = getAll<JsonAdapterEntry<*>>().toSet(),
            moshiBuilder = get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
        )
    }
}