package com.walletconnect.android.pairing

import com.walletconnect.foundation.common.model.Topic

interface PairingStorageRepository {

    fun insertPairing(pairing: Pairing)

    fun deletePairing(topic: Topic)

    fun isPairingValid(topic: Topic): Boolean

    fun getListOfPairingVOs(): List<Pairing>
}