package com.walletconnect.android.internal.common.dispacher

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.foundation.util.Logger

class EnvelopeDispatcher(
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer,
    private val logger: Logger
) {

}