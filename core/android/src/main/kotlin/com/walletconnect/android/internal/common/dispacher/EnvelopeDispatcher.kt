package com.walletconnect.android.internal.common.dispacher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.type.JsonRpcClientSync
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.foundation.util.Logger

class EnvelopeDispatcher(
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    private val serializer: JsonRpcSerializer,
    private val context: Context,
    private val logger: Logger
) {

    fun triggerRequest(payload: JsonRpcClientSync<*>) {
        val requestJson = serializer.serialize(payload)

        println("kobe: Request: $requestJson")
        //todo: historyy

        val encodedRequest = Base64.encodeToString(requestJson!!.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.NO_PADDING)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet?wc_ev=$encodedRequest")
                .also { println("kobe: URL: $it") }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}