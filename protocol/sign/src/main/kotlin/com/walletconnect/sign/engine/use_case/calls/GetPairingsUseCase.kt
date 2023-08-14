package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.model.mapper.toPairing
import com.walletconnect.sign.engine.model.EngineDO

internal class GetPairingsUseCase(private val pairingInterface: PairingInterface) : GetPairingsUseCaseInterface {

    override fun getListOfSettledPairings(): List<EngineDO.PairingSettle> {
        return pairingInterface.getPairings().map { pairing ->
            val mappedPairing = pairing.toPairing()
            EngineDO.PairingSettle(mappedPairing.topic, mappedPairing.peerAppMetaData)
        }
    }
}

internal interface GetPairingsUseCaseInterface {
    fun getListOfSettledPairings(): List<EngineDO.PairingSettle>
}