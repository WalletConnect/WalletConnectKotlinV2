package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.common.model.vo.clientsync.common.Caip222Request
import com.walletconnect.sign.engine.model.mapper.toCAIP122Message
import kotlinx.coroutines.supervisorScope

internal class FormatAuthenticateMessageUseCase : FormatAuthenticateMessageUseCaseInterface {
    override suspend fun formatMessage(caip222Request: Caip222Request, iss: String): String = supervisorScope {
        val issuer = Issuer(iss)
        if (!caip222Request.chains.contains(issuer.chainId)) throw Exception()//InvalidParamsException("Issuer chainId does not match with PayloadParams")
        caip222Request.chains.forEach { chainId -> if (!CoreValidator.isChainIdCAIP2Compliant(chainId)) throw Exception() }
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw Exception() //throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw Exception()// throw InvalidParamsException("Issuer address is not CAIP-10 compliant")

        //todo: hardcoded Ethereum?
        return@supervisorScope caip222Request.toCAIP122Message(issuer)
    }
}

internal interface FormatAuthenticateMessageUseCaseInterface {
    suspend fun formatMessage(caip222Request: Caip222Request, iss: String): String
}