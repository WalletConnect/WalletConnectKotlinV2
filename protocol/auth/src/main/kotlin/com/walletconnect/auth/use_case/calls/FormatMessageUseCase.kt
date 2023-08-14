package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.auth.common.exceptions.InvalidParamsException
import com.walletconnect.auth.common.model.PayloadParams
import com.walletconnect.auth.engine.mapper.toCAIP122Message

internal class FormatMessageUseCase : FormatMessageUseCaseInterface {
    override fun formatMessage(payloadParams: PayloadParams, iss: String): String {
        val issuer = Issuer(iss)
        if (issuer.chainId != payloadParams.chainId) throw InvalidParamsException("Issuer chainId does not match with PayloadParams")
        if (!CoreValidator.isChainIdCAIP2Compliant(payloadParams.chainId)) throw InvalidParamsException("PayloadParams chainId is not CAIP-2 compliant")
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw InvalidParamsException("Issuer address is not CAIP-10 compliant")

        return payloadParams.toCAIP122Message(issuer)
    }
}

internal interface FormatMessageUseCaseInterface {
    fun formatMessage(payloadParams: PayloadParams, iss: String): String
}