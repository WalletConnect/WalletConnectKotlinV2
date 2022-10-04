@file:JvmSynthetic

package com.walletconnect.android.impl.storage.dao

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.Expiry
import com.walletconnect.android.common.model.Pairing
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.impl.storage.data.dao.PairingQueries
import com.walletconnect.foundation.common.model.Topic

class PairingDao(private val pairingQueries: PairingQueries) {

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun insertOrAbortPairing(pairing: Pairing): Unit = with(pairing) {
        pairingQueries.insertOrAbortPairing(topic.value, expiry.seconds, relayProtocol, relayData, uri, isActive)
    }

    @JvmSynthetic
    fun deletePairing(topic: Topic): Unit = pairingQueries.deletePairing(topic.value)

    @JvmSynthetic
    fun getListOfPairing(): List<Pairing> = pairingQueries.getListOfPairing(mapper = this::toPairing).executeAsList()

    @JvmSynthetic
    fun getPairingByTopic(topic: Topic): Pairing = pairingQueries.getPairingByTopic(topic.value, mapper = this::toPairing).executeAsOne()

    @JvmSynthetic
    fun existsByTopic(topic: Topic): Boolean = pairingQueries.hasTopic(topic.value).executeAsOneOrNull() != null

    @JvmSynthetic
    fun getExpiry(topic: Topic): Expiry = Expiry(pairingQueries.getExpiry(topic.value).executeAsOne())

    @JvmSynthetic
    @Throws(SQLiteException::class)
    fun updateOrAbortExpiry(expiry: Expiry, topic: Topic) = pairingQueries.updateOrAbortExpiry(expiry.seconds, topic.value)

    @JvmSynthetic
    fun activatePairing(topic: Topic, expiryInSeconds: Long) {
        pairingQueries.activatePairing(expiryInSeconds, true, topic.value)
    }

    private fun toPairing(
        topic: String, expirySeconds: Long, relay_protocol: String, relay_data: String?, uri: String, is_active: Boolean,
        peerName: String?, peerDesc: String?, peerUrl: String?, peerIcons: List<String>?, native: String?,
    ): Pairing {
        val peerMetaData: PeerMetaData? = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            PeerMetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
        } else {
            null
        }

        return Pairing(topic = Topic(topic), expiry = Expiry(expirySeconds), peerMetaData = peerMetaData, relayProtocol = relay_protocol, relayData = relay_data, uri = uri, isActive = is_active)
    }
}