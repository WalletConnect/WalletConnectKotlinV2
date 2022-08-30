@file:JvmSynthetic

package com.walletconnect.auth.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.impl.common.model.Expiry
import com.walletconnect.android.impl.common.model.MetaData
import com.walletconnect.auth.common.PairingVO
import com.walletconnect.auth.storage.data.dao.MetaDataDaoQueries
import com.walletconnect.auth.storage.data.dao.PairingDaoQueries
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.isSequenceValid

internal class AuthStorageRepository(private val pairingDaoQueries: PairingDaoQueries, private val metaDataDaoQueries: MetaDataDaoQueries) {

    @JvmSynthetic
    var onPairingExpired: (topic: Topic) -> Unit = {}

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertPairing(pairing: PairingVO) {
        with(pairing) {
            pairingDaoQueries.insertOrAbortPairing(
                topic.value,
                expiry.seconds,
                relayProtocol,
                relayData,
                uri,
                isActive
            )
        }
    }

    @JvmSynthetic
    fun deletePairing(topic: Topic) {
        metaDataDaoQueries.deleteMetaDataFromTopic(topic.value)
        pairingDaoQueries.deletePairing(topic.value)
    }

    @JvmSynthetic
    fun getListOfPairingVOs(): List<PairingVO> =
        pairingDaoQueries.getListOfPairingDaos(mapper = this::mapPairingDaoToPairingVO).executeAsList()

    fun isPairingValid(topic: Topic): Boolean {
        val hasTopic = pairingDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        return if (hasTopic) {
            val expiry = pairingDaoQueries.getExpiry(topic.value).executeAsOne()
            verifyExpiry(expiry, topic) { pairingDaoQueries.deletePairing(topic.value) }
        } else {
            false
        }
    }

    private fun mapPairingDaoToPairingVO(
        topic: String,
        expirySeconds: Long,
        relay_protocol: String,
        relay_data: String?,
        uri: String,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?,
        is_active: Boolean,
    ): PairingVO {
        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            MetaData(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        return PairingVO(
            topic = Topic(topic),
            expiry = Expiry(expirySeconds),
            peerMetaData = peerMetaData,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            uri = uri,
            isActive = is_active
        )
    }

    private fun verifyExpiry(expiry: Long, topic: Topic, deleteSequence: () -> Unit): Boolean {
        return if (Expiry(expiry).isSequenceValid()) {
            true
        } else {
            deleteSequence()
            onPairingExpired(topic)
            false
        }
    }
}