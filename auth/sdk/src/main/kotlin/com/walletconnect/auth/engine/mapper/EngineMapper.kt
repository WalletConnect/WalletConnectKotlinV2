package com.walletconnect.auth.engine.mapper

import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.common.signing.cacao.toCAIP122Message
import com.walletconnect.android.verify.data.model.VerifyContext
import com.walletconnect.auth.common.model.AuthContext
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
internal fun JsonRpcHistoryEntry.toPendingRequest(): PendingRequest = PendingRequest(id, topic.value, params.payloadParams)

@JvmSynthetic
internal fun VerifyContext.toAuthContext(): AuthContext =
    AuthContext(origin, validation, verifyUrl)