package com.walletconnect.android.internal.common.storage.pairing

import android.database.sqlite.SQLiteException
import app.cash.sqldelight.async.coroutines.awaitAsList
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.sdk.storage.data.dao.PairingQueries
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.utils.Empty

class PairingStorageRepository(private val pairingQueries: PairingQueries) : PairingStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertPairing(pairing: Pairing) {
        with(pairing) {
            pairingQueries.insertOrAbortPairing(
                topic = topic.value,
                expiry = expiry.seconds,
                relay_protocol = relayProtocol,
                relay_data = relayData,
                uri = uri,
                methods = methods ?: String.Empty,
                is_active = true,
                is_proposal_received = isProposalReceived
            )
        }
    }

    @Throws(SQLiteException::class)
    override fun deletePairing(topic: Topic) {
        pairingQueries.deletePairing(topic.value)
    }

    override fun hasTopic(topic: Topic): Boolean = pairingQueries.hasTopic(topic = topic.value).executeAsOneOrNull() != null

    @Throws(SQLiteException::class)
    override suspend fun getListOfPairings(): List<Pairing> = pairingQueries.getListOfPairing(mapper = this::toPairing).awaitAsList()

    @Throws(SQLiteException::class)
    override suspend fun getListOfPairingsWithoutRequestReceived(): List<Pairing> = pairingQueries.getListOfPairingsWithoutRequestReceived(mapper = this::toPairing).awaitAsList()

    @Throws(SQLiteException::class)
    override fun setRequestReceived(topic: Topic) {
        pairingQueries.setRequestReceived(is_proposal_received = true, topic = topic.value)
    }

    @Throws(SQLiteException::class)
    override fun updateExpiry(topic: Topic, expiry: Expiry): Unit = pairingQueries.updateOrAbortExpiry(expiry = expiry.seconds, topic = topic.value)

    @Throws(SQLiteException::class)
    override fun getPairingOrNullByTopic(topic: Topic): Pairing? = pairingQueries.getPairingByTopic(topic = topic.value, mapper = this::toPairing).executeAsOneOrNull()

    private fun toPairing(
        topic: String,
        expirySeconds: Long,
        relay_protocol: String,
        relay_data: String?,
        uri: String,
        methods: String,
        is_proposal_received: Boolean?,
        peerName: String?,
        peerDesc: String?,
        peerUrl: String?,
        peerIcons: List<String>?,
        native: String?
    ): Pairing {
        val peerAppMetaData: AppMetaData? = if (peerName != null && peerDesc != null && peerUrl != null && peerIcons != null) {
            AppMetaData(name = peerName, description = peerDesc, url = peerUrl, icons = peerIcons, redirect = Redirect(native = native))
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
            isProposalReceived = is_proposal_received ?: true,
            methods = methods
        )
    }
}