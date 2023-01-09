package com.walletconnect.android.internal.common.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.pairing.model.ACTIVE_PAIRING
import com.walletconnect.android.sdk.storage.data.dao.PairingQueries
import com.walletconnect.foundation.common.model.Topic

class PairingStorageRepository(private val pairingQueries: PairingQueries) : PairingStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertPairing(pairing: Pairing) {
        with(pairing) {
            pairingQueries.insertOrAbortPairing(topic.value, expiry.seconds, relayProtocol, relayData, uri, registeredMethods, isActive)
        }
    }

    @Throws(SQLiteException::class)
    override fun deletePairing(topic: Topic) {
        pairingQueries.deletePairing(topic.value)
    }

    override fun hasTopic(topic: Topic): Boolean = pairingQueries.hasTopic(topic.value).executeAsOneOrNull() != null

    @Throws(SQLiteException::class)
    override fun getListOfPairings(): List<Pairing> = pairingQueries.getListOfPairing(mapper = this::toPairing).executeAsList()

    @Throws(SQLiteException::class)
    override fun activatePairing(topic: Topic) = pairingQueries.activatePairing(ACTIVE_PAIRING, true, topic.value)

    @Throws(SQLiteException::class)
    override fun updateExpiry(topic: Topic, expiry: Expiry): Unit = pairingQueries.updateOrAbortExpiry(expiry.seconds, topic.value)

    @Throws(SQLiteException::class)
    override fun getPairingOrNullByTopic(topic: Topic): Pairing? = pairingQueries.getPairingByTopic(topic.value, mapper = this::toPairing).executeAsOneOrNull()

    private fun toPairing(
        topic: String,
        expirySeconds: Long,
        relay_protocol: String,
        relay_data: String?,
        uri: String,
        methods: String,
        is_active: Boolean,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?,
        native: String?,
    ): Pairing {
        val peerAppMetaData: AppMetaData? = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            AppMetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
        } else {
            null
        }

        return Pairing(
            topic = Topic(topic),
            expiry = Expiry(expirySeconds),
            peerAppMetaData = peerAppMetaData,
            relayProtocol = relay_protocol,
            relayData = relay_data,
            uri = uri,
            registeredMethods = methods,
            isActive = is_active
        )
    }
}