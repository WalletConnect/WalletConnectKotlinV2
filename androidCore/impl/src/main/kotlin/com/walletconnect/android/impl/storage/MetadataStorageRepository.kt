package com.walletconnect.android.impl.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.MetaDataType
import com.walletconnect.android.common.model.MetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.impl.storage.data.dao.MetaDataQueries
import com.walletconnect.foundation.common.model.Topic

class MetadataStorageRepository(private val metaDataQueries: MetaDataQueries): MetadataStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertOrAbortMetadata(topic: Topic, metaData: MetaData, metaDataType: MetaDataType) = with(metaData) {
        metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, metaDataType)
    }

    @Throws(SQLiteException::class)
    override fun updateOrAbortMetaData(topic: Topic, metaData: MetaData, metaDataType: MetaDataType) = with(metaData) {
        metaDataQueries.updateOrAbortMetaData(name, description, url, icons, redirect?.native, metaDataType, topic.value)
    }

    @Throws(SQLiteException::class)
    override fun upsertPairingPeerMetadata(topic: Topic, metaData: MetaData, metaDataType: MetaDataType) {
        if (!existsByTopic(topic)) {
            insertOrAbortMetadata(topic, metaData, metaDataType)
        } else {
            updateOrAbortMetaData(topic, metaData, metaDataType)
        }
    }

    override fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    override fun existsByTopic(topic: Topic): Boolean = metaDataQueries.getIdByTopic(topic.value).executeAsOneOrNull() == null

    override fun getByTopic(topic: Topic): MetaData = metaDataQueries.getMetadataByTopic(topic.value, mapper = this::toMetadata).executeAsOne()

    private fun toMetadata(peerName: String, peerDesc: String, peerUrl: String, peerIcons: List<String>, native: String?): MetaData =
        MetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
}