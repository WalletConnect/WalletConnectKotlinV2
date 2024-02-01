package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod

internal class GetPairingForSessionAuthenticate(private var pairingProtocol: PairingInterface) : GetPairingForSessionAuthenticateInterface {
    override suspend fun getPairingForSessionAuthenticate(pairingTopic: String?): Core.Model.Pairing {
        val pairing: Core.Model.Pairing = if (pairingTopic != null) {
            pairingProtocol.getPairings().find { pairing -> pairing.topic == pairingTopic } ?: throw Exception("Pairing does not exist")
        } else {
            pairingProtocol.create(method = JsonRpcMethod.WC_SESSION_AUTHENTICATE, onError = { error -> throw error.throwable }) ?: throw Exception("Cannot create a pairing")
        }
        if (!pairing.registeredMethods.contains(JsonRpcMethod.WC_SESSION_AUTHENTICATE)) {
            throw Exception("Pairing does not support wc_sessionAuthenticate")
        }
        return pairing
    }
}

internal interface GetPairingForSessionAuthenticateInterface {
    suspend fun getPairingForSessionAuthenticate(pairingTopic: String?): Core.Model.Pairing
}