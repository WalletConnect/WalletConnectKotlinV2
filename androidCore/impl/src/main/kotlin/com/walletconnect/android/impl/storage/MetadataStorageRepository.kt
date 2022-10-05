package com.walletconnect.android.impl.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.PeerMetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic

class MetadataStorageRepository(private val metaDataQueries: MetaDataQueries): MetadataStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertOrAbortMetadata(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) = with(metaData) {
        metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, metaDataType)
    }

    @Throws(SQLiteException::class)
    override fun updateOrAbortMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic) = with(metaData) {
        metaDataQueries.updateOrAbortMetaData(name, description, url, icons, redirect?.native, metaDataType, topic.value)
    }

    override fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    override fun existsByTopic(topic: Topic): Boolean = metaDataQueries.getIdByTopic(topic.value).executeAsOneOrNull() == null

    override fun getByTopic(topic: Topic): PeerMetaData = metaDataQueries.getMetadataByTopic(topic.value, mapper = this::toMetadata).executeAsOne()

//    @Throws(SQLiteException::class)
//    override fun updateMetadata(topic: Topic, metadata: PeerMetaData): Unit = metadataQueries.updateOrAbortMetaData(metadata, MetaDataType.PEER, topic) // todo is MetaDataType.PEER necessary?
//
//    @Throws(SQLiteException::class)
//    override fun upsertPairingPeerMetadata(topic: Topic, metaData: PeerMetaData) {
//        if (!metadataDao.existsByTopic(topic)) {
//            insertMetaData(metaData, MetaDataType.PEER, topic)
//        } else {
//            metadataDao.updateOrAbortMetaData(metaData, MetaDataType.PEER, topic)
//        }
//    }
//
//    @Throws(SQLiteException::class)
//    private fun insertMetaData(metaData: PeerMetaData, metaDataType: MetaDataType, topic: Topic): Unit = metadataDao.insertOrAbortMetadata(metaData, metaDataType, topic)

    private fun toMetadata(peerName: String, peerDesc: String, peerUrl: String, peerIcons: List<String>, native: String?): PeerMetaData =
        PeerMetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
}