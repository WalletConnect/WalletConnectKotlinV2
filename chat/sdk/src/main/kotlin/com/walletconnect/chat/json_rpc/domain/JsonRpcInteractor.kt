package com.walletconnect.chat.json_rpc.domain

import com.walletconnect.android.RelayConnectionInterface
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.json_rpc.domain.BaseJsonRpcInteractor
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.chat.json_rpc.data.JsonRpcSerializer

internal class JsonRpcInteractor(
    relay: RelayConnectionInterface,
    chaChaPolyCodec: Codec,
    jsonRpcHistory: JsonRpcHistory,
    serializer: JsonRpcSerializer
) : BaseJsonRpcInteractor(relay, serializer, chaChaPolyCodec, jsonRpcHistory)
