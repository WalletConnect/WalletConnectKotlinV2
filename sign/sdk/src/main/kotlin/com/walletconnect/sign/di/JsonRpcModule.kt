@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {

//    includes(coreJsonRpcModule())

    addSerializerEntry(SignRpc.SessionPropose::class)
    addSerializerEntry(SignRpc.SessionPing::class)
    addSerializerEntry(SignRpc.SessionEvent::class)
    addSerializerEntry(SignRpc.SessionUpdate::class)
    addSerializerEntry(SignRpc.SessionRequest::class)
    addSerializerEntry(SignRpc.SessionDelete::class)
    addSerializerEntry(SignRpc.SessionSettle::class)
    addSerializerEntry(SignRpc.SessionExtend::class)

    addDeserializerEntry(JsonRpcMethod.WC_SESSION_PROPOSE, SignRpc.SessionPropose::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_SETTLE, SignRpc.SessionSettle::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_REQUEST, SignRpc.SessionRequest::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_DELETE, SignRpc.SessionDelete::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_PING, SignRpc.SessionPing::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_EVENT, SignRpc.SessionEvent::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_UPDATE, SignRpc.SessionUpdate::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_EXTEND, SignRpc.SessionExtend::class)
}