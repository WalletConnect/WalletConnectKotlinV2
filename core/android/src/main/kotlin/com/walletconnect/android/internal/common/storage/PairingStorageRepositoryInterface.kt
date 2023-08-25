package com.walletconnect.android.internal.common.storage

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.foundation.common.model.Topic

interface PairingStorageRepositoryInterface {

    fun insertPairing(pairing: Pairing)

    fun deletePairing(topic: Topic)

    fun hasTopic(topic: Topic): Boolean

    fun getListOfPairings(): List<Pairing>

    fun activatePairing(topic: Topic)

    fun updateExpiry(topic: Topic, expiry: Expiry)

    fun getPairingOrNullByTopic(topic: Topic): Pairing?

    fun markAsReceived(topic: Topic)
}