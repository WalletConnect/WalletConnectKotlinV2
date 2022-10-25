@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpcVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule
@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    addSerializerEntry(SignRpcVO.SessionPropose::class)
    addSerializerEntry(SignRpcVO.SessionPing::class)
    addSerializerEntry(SignRpcVO.SessionEvent::class)
    addSerializerEntry(SignRpcVO.SessionUpdate::class)
    addSerializerEntry(SignRpcVO.SessionRequest::class)
    addSerializerEntry(SignRpcVO.SessionDelete::class)
    addSerializerEntry(SignRpcVO.SessionSettle::class)
    addSerializerEntry(SignRpcVO.SessionExtend::class)

    addDeserializerEntry(JsonRpcMethod.WC_SESSION_PROPOSE, SignRpcVO.SessionPropose::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_SETTLE, SignRpcVO.SessionSettle::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_REQUEST, SignRpcVO.SessionRequest::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_DELETE, SignRpcVO.SessionDelete::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_PING, SignRpcVO.SessionPing::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_EVENT, SignRpcVO.SessionEvent::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_UPDATE, SignRpcVO.SessionUpdate::class)
    addDeserializerEntry(JsonRpcMethod.WC_SESSION_EXTEND, SignRpcVO.SessionExtend::class)
}