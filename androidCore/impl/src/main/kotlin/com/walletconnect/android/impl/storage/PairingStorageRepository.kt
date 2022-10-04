package com.walletconnect.android.impl.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.ACTIVE_PAIRING
import com.walletconnect.android.common.CURRENT_TIME_IN_SECONDS
import com.walletconnect.android.common.model.Expiry
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.Pairing
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.android.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.impl.common.scope.scope
import com.walletconnect.android.impl.storage.dao.MetadataDao
import com.walletconnect.android.impl.storage.dao.PairingDao
import com.walletconnect.foundation.common.model.Topic
import kotlinx.coroutines.flow.*

class PairingStorageRepository(val metadataDao: MetadataDao, val pairingDao: PairingDao) : PairingStorageRepositoryInterface {

    private val _topicExpiredFlow: MutableSharedFlow<Topic> = MutableSharedFlow()
    override val topicExpiredFlow: SharedFlow<Topic> = _topicExpiredFlow.onEach { deletePairing(it) }.shareIn(scope, SharingStarted.Lazily)

    @Throws(SQLiteException::class)
    override fun insertPairing(pairing: Pairing) {
        pairingDao.insertOrAbortPairing(pairing)
    }

    @Throws(SQLiteException::class)
    override fun deletePairing(topic: Topic) {
        pairingDao.deletePairing(topic)
        metadataDao.deleteMetaData(topic)
    }

    @Throws(SQLiteException::class)
    override fun isPairingValid(topic: Topic): Boolean =
        if (pairingDao.existsByTopic(topic)) {
            if (pairingDao.getExpiry(topic).seconds > CURRENT_TIME_IN_SECONDS) {
                true
            } else {
                _topicExpiredFlow.tryEmit(topic)
                false
            }
        } else {
            false
        }

    @Throws(SQLiteException::class)
    override fun getListOfPairings(): List<Pairing> = pairingDao.getListOfPairing()

    @Throws(SQLiteException::class)
    override fun activatePairing(topic: Topic): Unit = pairingDao.activatePairing(topic, ACTIVE_PAIRING)

    @Throws(SQLiteException::class)
    override fun updateExpiry(topic: Topic, expiry: Expiry): Unit = pairingDao.updateOrAbortExpiry(expiry, topic)

    @Throws(SQLiteException::class)
    override fun updateMetadata(topic: Topic, metadata: PeerMetaData): Unit = metadataDao.updateOrAbortMetaData(metadata, MetaDataType.PEER, topic) // todo is MetaDataType.PEER necessary?

    @Throws(SQLiteException::class)
    override fun getPairingOrNullByTopic(topic: Topic): Pairing? = pairingDao.getPairingOrNullByTopic(topic)

    @Throws(SQLiteException::class)
    override fun upsertPairingPeerMetadata(topic: Topic, metaData: PeerMetaData) {
        if (!metadataDao.existsByTopic(topic)) {
            insertMetaData(metaData, MetaDataType.PEER, topic)
        } else {
            metadataDao.updateOrAbortMetaData(metaData, MetaDataType.PEER, topic)
        }
    }

    @Throws(SQLiteException::class)
    private fun insertMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic): Unit = metadataDao.insertOrAbortMetadata(metaData, metaDataType, topic)

}