@file:JvmSynthetic

package com.walletconnect.auth.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.metadata.PeerMetaData
import com.walletconnect.android.common.model.pairing.Expiry
import com.walletconnect.auth.storage.data.dao.MetaDataDaoQueries
import com.walletconnect.auth.common.model.Pairing
import com.walletconnect.auth.storage.data.dao.PairingDaoQueries
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.isNotExpired

internal class AuthStorageRepository(private val pairingDaoQueries: PairingDaoQueries, private val metaDataDaoQueries: MetaDataDaoQueries) {

    @JvmSynthetic
    var onPairingExpired: (topic: Topic) -> Unit = {}

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertPairing(pairing: Pairing) {
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
    fun getListOfPairingVOs(): List<Pairing> =
        pairingDaoQueries.getListOfPairingDaos(mapper = this::mapPairingDaoToPairingVO).executeAsList()

    @JvmSynthetic
    fun getPairingByTopic(topic: Topic): Pairing =
        pairingDaoQueries.getPairingByTopic(topic.value).executeAsOne().let { entity ->
            Pairing(
                topic = Topic(entity.topic),
                expiry = Expiry(entity.expiry),
                uri = entity.uri,
                relayProtocol = entity.relay_protocol,
                relayData = entity.relay_data,
                isActive = entity.is_active
            )
        }

    @JvmSynthetic
    fun isPairingValid(topic: Topic): Boolean {
        val hasTopic = pairingDaoQueries.hasTopic(topic.value).executeAsOneOrNull() != null

        return if (hasTopic) {
            val expiry = pairingDaoQueries.getExpiry(topic.value).executeAsOne()
            verifyExpiry(expiry, topic) { pairingDaoQueries.deletePairing(topic.value) }
        } else {
            false
        }
    }

    @JvmSynthetic
    fun activatePairing(topic: Topic, expiryInSeconds: Long) {
        pairingDaoQueries.activatePairing(expiryInSeconds, true, topic.value)
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
    ): Pairing {
        val peerMetaData = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            PeerMetaData(peerName, peerDesc, peerUrl, peerIcons)
        } else {
            null
        }

        return Pairing(
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
        return if (Expiry(expiry).isNotExpired()) {
            true
        } else {
            deleteSequence()
            onPairingExpired(topic)
            false
        }
    }
}