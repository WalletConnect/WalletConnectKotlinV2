package com.walletconnect.android.common.pairing

import com.walletconnect.android.common.constants.ACTIVE_PAIRING
import com.walletconnect.android.common.constants.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.metadata.PeerMetaData
import com.walletconnect.android.common.model.pairing.Expiry
import com.walletconnect.android.common.model.pairing.Pairing
import com.walletconnect.android.common.storage.MetadataDao
import com.walletconnect.android.common.storage.PairingDao
import com.walletconnect.foundation.common.model.Topic


//todo: what about SQLExceptions?
class PairingStorageRepository(private val pairingDao: PairingDao, private val metadataDao: MetadataDao) {


    ///todo: improve
    @JvmSynthetic
    fun onPairingExpired(onExpired: (Topic) -> Unit) {
        
    }

    @JvmSynthetic
    fun insertPairing(pairing: Pairing): Unit = pairingDao.insertOrAbortPairing(pairing)

    @JvmSynthetic
    fun deletePairing(topic: Topic) {
        pairingDao.deletePairing(topic)
        metadataDao.deleteMetaData(topic)
    }

    @JvmSynthetic
    fun activatePairing(topic: Topic): Unit = pairingDao.activatePairing(topic, ACTIVE_PAIRING)

    @JvmSynthetic
    fun updateExpiry(topic: Topic, expiry: Expiry): Unit = pairingDao.updateOrAbortExpiry(expiry, topic)

    @JvmSynthetic
    fun updateMetadata(topic: Topic, metadata: PeerMetaData): Unit = metadataDao.updateOrAbortMetaData(metadata, MetaDataType.PEER, topic)

    @JvmSynthetic
    fun getListOfPairing(): List<Pairing> = pairingDao.getListOfPairing()

    @JvmSynthetic
    fun getPairingByTopic(topic: Topic): Pairing = pairingDao.getPairingByTopic(topic)

    @JvmSynthetic
    fun isPairingExpiredAndClearExpired(topic: Topic): Boolean =
        if (pairingDao.existsByTopic(topic)) {
            if (pairingDao.getExpiry(topic).seconds > CURRENT_TIME_IN_SECONDS) {
                true
            } else {
                clearExpiredPairing(topic)
                false
            }
        } else {
            false
        }


    private fun clearExpiredPairing(topic: Topic) {
        deletePairing(topic)
        ///todo: improve
//        onPairingExpired(topic)
    }
}