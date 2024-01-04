package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.signing.cacao.Issuer
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.sign.common.ATT_KEY
import com.walletconnect.sign.common.RECAPS_PREFIX
import com.walletconnect.sign.common.model.vo.clientsync.common.PayloadParams
import com.walletconnect.sign.engine.model.mapper.toCAIP222Message
import kotlinx.coroutines.supervisorScope
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

internal class FormatAuthenticateMessageUseCase : FormatAuthenticateMessageUseCaseInterface {
    override suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String = supervisorScope {
        val issuer = Issuer(iss) //todo add iss validation

        //todo: add validation for one type of namespaces in payload chains and namespace MUST be in issuer

        if (!payloadParams.chains.contains(issuer.chainId)) throw Exception()//InvalidParamsException("Issuer chainId does not match with PayloadParams")
        payloadParams.chains.forEach { chainId -> if (!CoreValidator.isChainIdCAIP2Compliant(chainId)) throw Exception() }
        if (!CoreValidator.isChainIdCAIP2Compliant(issuer.chainId)) throw Exception() //throw InvalidParamsException("Issuer chainId is not CAIP-2 compliant")
        if (!CoreValidator.isAccountIdCAIP10Compliant(issuer.accountId)) throw Exception()// throw InvalidParamsException("Issuer address is not CAIP-10 compliant")

//urn:recap:eyJhdHQiOnsiZWlwMTU1IjpbeyJyZXF1ZXN0L3BlcnNvbmFsX3NpZ24iOltdfSx7InJlcXVlc3QvZXRoX3NpZ25UeXBlZERhdGFfdjQiOltdfV19fQ==

        val encodedReCaps = payloadParams.resources?.find { resource -> resource.startsWith(RECAPS_PREFIX) }?.removePrefix(RECAPS_PREFIX) ?: throw Exception()
        val reCaps = Base64.decode(encodedReCaps).toString(Charsets.UTF_8)
        println("dupa: $reCaps")
        val requests = (JSONObject(reCaps).get(ATT_KEY) as JSONObject).getJSONArray(issuer.namespace)
        val actions: MutableList<Pair<String, String>> = mutableListOf()

        for (i in 0 until requests.length()) {
            val actionString = requests.getJSONObject(i).keys().next() as String
            val actionType = actionString.split(ACTION_DELIMITER)[ACTION_TYPE_POSITION]
            val action = actionString.split(ACTION_DELIMITER)[ACTION_POSITION]
            actions.add(actionType to action)
        }
        val actionsString = actions.joinToString(separator = ",") { action -> "'${action.first}': '${action.second}'" }

//todo: hardcoded Ethereum? add new method for caip222
        //todo: Figure out dynamic chain name
        return@supervisorScope payloadParams.toCAIP222Message(issuer, actionsString, "Ethereum")
    }

    private companion object {
        const val ACTION_TYPE_POSITION = 0
        const val ACTION_POSITION = 1
        const val ACTION_DELIMITER = "/"
    }
}

internal interface FormatAuthenticateMessageUseCaseInterface {
    suspend fun formatMessage(payloadParams: PayloadParams, iss: String): String
}