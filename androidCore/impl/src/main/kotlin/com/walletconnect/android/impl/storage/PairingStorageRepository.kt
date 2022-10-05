package com.walletconnect.android.impl.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.ACTIVE_PAIRING
import com.walletconnect.android.common.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.common.model.Expiry
import com.walletconnect.android.common.model.Pairing
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.*

class PairingStorageRepository(private val pairingQueries: PairingQueries) : PairingStorageRepositoryInterface {

    private val _topicExpiredFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    override val topicExpiredFlow: SharedFlow<Topic> = _topicExpiredFlow.onEach { deletePairing(it) }.shareIn(scope, SharingStarted.Lazily)

    @Throws(SQLiteException::class)
    override fun insertPairing(pairing: Pairing) {
        with(pairing) {
            pairingQueries.insertOrAbortPairing(topic.value, expiry.seconds, relayProtocol, relayData, uri, isActive)
        }
    }

    @Throws(SQLiteException::class)
    override fun deletePairing(topic: Topic) {
        pairingQueries.deletePairing(topic.value)
    }

    @Throws(SQLiteException::class)
    override fun isPairingValid(topic: Topic): Boolean {
        return if (pairingQueries.hasTopic(topic.value).executeAsOneOrNull() != null) {
            if (Expiry(pairingQueries.getExpiry(topic.value).executeAsOne()).seconds > CURRENT_TIME_IN_SECONDS) {
                true
            } else {
                _topicExpiredFlow.tryEmit(topic)
                false
            }
        } else {
            false
        }
    }

    @Throws(SQLiteException::class)
    override fun getListOfPairings(): List<Pairing> = pairingQueries.getListOfPairing(mapper = this::toPairing).executeAsList()

    @Throws(SQLiteException::class)
    override fun activatePairing(topic: Topic) = pairingQueries.activatePairing(ACTIVE_PAIRING, true, topic.value)

    @Throws(SQLiteException::class)
    override fun updateExpiry(topic: Topic, expiry: Expiry): Unit = pairingQueries.updateOrAbortExpiry(expiry.seconds, topic.value)

    @Throws(SQLiteException::class)
    override fun getPairingOrNullByTopic(topic: Topic): Pairing? = pairingQueries.getPairingByTopic(topic.value, mapper = this::toPairing).executeAsOneOrNull()

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