package com.walletconnect.android.internal.common.di

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.connection.ConnectionLifecycle
import com.walletconnect.android.internal.common.connection.DefaultConnectionLifecycle
import com.walletconnect.android.internal.common.connection.ManualConnectionLifecycle
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractor
import com.walletconnect.android.internal.common.json_rpc.domain.link_mode.LinkModeJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.json_rpc.domain.relay.RelayJsonRpcInteractor
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.model.type.SerializableJsonRpc
import com.walletconnect.android.pairing.model.PairingJsonRpcMethod
import com.walletconnect.android.pairing.model.PairingRpc
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.utils.JsonAdapterEntry
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import kotlin.reflect.KClass


fun Scope.getConnectionLifecycle(connectionType: ConnectionType): ConnectionLifecycle =
    if (connectionType == ConnectionType.MANUAL) {
        get<ManualConnectionLifecycle>(named(AndroidCommonDITags.MANUAL_CONNECTION_LIFECYCLE))
    } else {
        get<DefaultConnectionLifecycle>(named(AndroidCommonDITags.DEFAULT_CONNECTION_LIFECYCLE))
    }

@JvmSynthetic
fun coreJsonRpcModule(connectionType: ConnectionType) = module {

    single<RelayJsonRpcInteractorInterface> {
        RelayJsonRpcInteractor(
            relay = get(),
            chaChaPolyCodec = get(),
            jsonRpcHistory = get(),
            pushMessageStorage = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            connectionLifecycle = getConnectionLifecycle(connectionType),
            backoffStrategy = get()
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

    single<LinkModeJsonRpcInteractorInterface> {
        LinkModeJsonRpcInteractor(
            chaChaPolyCodec = get(),
            jsonRpcHistory = get(),
            context = androidContext()
        )
    }
}