package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.common.model.vo.clientsync.common.PayloadParams
import com.walletconnect.sign.engine.model.mapper.toCAIP222Message
import kotlinx.coroutines.supervisorScope

internal class FormatAuthenticateMessageUseCase : FormatAuthenticateMessageUseCaseInterface {
    override suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String = supervisorScope {
        val issuer = Issuer(iss)

        //todo: add validation for one type of namespaces in payload chains and namespace MUST be in issuer
        if (!payloadParams.chains.contains(issuer.chainId)) throw Exception("Issuer chainId does not match with PayloadParams")
        payloadParams.chains.forEach { chainId -> if (!CoreValidator.isChainIdCAIP2Compliant(chainId)) throw Exception("Chains must be CAIP-2 compliant") }
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw Exception("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw Exception("Issuer address is not CAIP-10 compliant")

        //todo: Figure out dynamic chain name
        return@supervisorScope payloadParams.toCAIP222Message(issuer, "Ethereum")
    }
}

internal interface FormatAuthenticateMessageUseCaseInterface {
    suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String
}