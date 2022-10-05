package com.walletconnect.android.common.storage

import com.walletconnect.android.common.model.Expiry
import com.walletconnect.android.common.model.Pairing
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.SharedFlow

//todo: seperate interface might not be necessary
interface PairingStorageRepositoryInterface {

    val topicExpiredFlow : SharedFlow<Topic>

    fun insertPairing(pairing: Pairing)

    fun deletePairing(topic: Topic)

    fun isPairingValid(topic: Topic): Boolean

    fun hasTopic(topic: Topic): Boolean

    fun getListOfPairings(): List<Pairing>

    fun activatePairing(topic: Topic)

    fun updateExpiry(topic: Topic, expiry: Expiry)

    fun getPairingOrNullByTopic(topic: Topic): Pairing?
}