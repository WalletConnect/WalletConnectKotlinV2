@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.common.SerializableJsonRpc
import com.walletconnect.sign.common.model.vo.clientsync.pairing.PairingRpcVO
import com.walletconnect.sign.common.model.vo.clientsync.session.SessionRpcVO
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.utils.intoMultibindingMap
import com.walletconnect.utils.intoMultibindingSet
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule

@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionPing }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionEvent }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionUpdate }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionExtend }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionRequest }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionDelete }

    intoMultibindingSet { payload: SerializableJsonRpc -> payload is SessionRpcVO.SessionSettle }

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_SETTLE, SessionRpcVO.SessionSettle::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_REQUEST, SessionRpcVO.SessionRequest::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_DELETE, SessionRpcVO.SessionDelete::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_PING, SessionRpcVO.SessionPing::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_EVENT, SessionRpcVO.SessionEvent::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_UPDATE, SessionRpcVO.SessionUpdate::class)

    intoMultibindingMap(JsonRpcMethod.WC_SESSION_EXTEND, SessionRpcVO.SessionExtend::class)
}