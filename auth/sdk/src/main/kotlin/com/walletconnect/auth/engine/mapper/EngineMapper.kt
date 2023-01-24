package com.walletconnect.auth.engine.mapper

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.internal.common.cacao.Issuer
import com.walletconnect.android.internal.common.cacao.toCAIP122Message
import com.walletconnect.auth.common.model.JsonRpcHistoryEntry
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.common.model.PendingRequest

@JvmSynthetic
internal fun PayloadParams.toCacaoPayload(iss: Issuer): Cacao.Payload = Cacao.Payload(
    iss.value,
    domain = domain,
    aud = aud,
    version = version,
    nonce = nonce,
    iat = iat,
    nbf = nbf,
    exp = exp,
    statement = statement,
    requestId = requestId,
    resources = resources
)

@JvmSynthetic
internal fun PayloadParams.toCAIP122Message(iss: Issuer, chainName: String = "Ethereum"): String =
    this.toCacaoPayload(iss).toCAIP122Message(chainName)

@JvmSynthetic
internal fun JsonRpcHistoryEntry.toPendingRequest(): PendingRequest = PendingRequest(id, params.payloadParams)