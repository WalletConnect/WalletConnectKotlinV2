@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.internal.common.SerializableJsonRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpcVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.intoMultibindingMap
import com.walletconnect.utils.intoMultibindingSet
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule
@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    intoMultibindingSet(SignRpcVO.SessionPropose::class)
    intoMultibindingSet(SignRpcVO.SessionPing::class)
    intoMultibindingSet(SignRpcVO.SessionEvent::class)
    intoMultibindingSet(SignRpcVO.SessionUpdate::class)
    intoMultibindingSet(SignRpcVO.SessionRequest::class)
    intoMultibindingSet(SignRpcVO.SessionDelete::class)
    intoMultibindingSet(SignRpcVO.SessionSettle::class)
    intoMultibindingSet(SignRpcVO.SessionExtend::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_PROPOSE, SignRpcVO.SessionPropose::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_SETTLE, SignRpcVO.SessionSettle::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_REQUEST, SignRpcVO.SessionRequest::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_DELETE, SignRpcVO.SessionDelete::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_PING, SignRpcVO.SessionPing::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_EVENT, SignRpcVO.SessionEvent::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_UPDATE, SignRpcVO.SessionUpdate::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_EXTEND, SignRpcVO.SessionExtend::class)
}