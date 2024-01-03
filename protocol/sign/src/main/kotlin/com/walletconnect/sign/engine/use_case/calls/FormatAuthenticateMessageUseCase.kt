package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.common.model.vo.clientsync.common.PayloadParams
import com.walletconnect.sign.engine.model.mapper.toCAIP122Message
import kotlinx.coroutines.supervisorScope

internal class FormatAuthenticateMessageUseCase : FormatAuthenticateMessageUseCaseInterface {
    override suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String = supervisorScope {
        val issuer = Issuer(iss)
        if (!payloadParams.chains.contains(issuer.chainId)) throw Exception()//InvalidParamsException("Issuer chainId does not match with PayloadParams")
        payloadParams.chains.forEach { chainId -> if (!CoreValidator.isChainIdCAIP2Compliant(chainId)) throw Exception() }
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw Exception() //throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw Exception()// throw InvalidParamsException("Issuer address is not CAIP-10 compliant")


        //TODO: decode recaps

        //todo: hardcoded Ethereum? add new method for caip222
        return@supervisorScope payloadParams.toCAIP122Message(issuer)
    }
}

internal interface FormatAuthenticateMessageUseCaseInterface {
    suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String
}