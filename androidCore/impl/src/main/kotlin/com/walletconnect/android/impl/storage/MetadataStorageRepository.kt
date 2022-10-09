package com.walletconnect.android.impl.storage

import android.database.sqlite.SQLiteException
import com.walletconnect.android.common.model.AppMetaDataType
import com.walletconnect.android.common.model.AppMetaData
import com.walletconnect.android.common.model.Redirect
import com.walletconnect.android.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.impl.storage.data.dao.MetaDataQueries
import com.walletconnect.foundation.common.model.Topic

class MetadataStorageRepository(private val metaDataQueries: MetaDataQueries): MetadataStorageRepositoryInterface {

    @Throws(SQLiteException::class)
    override fun insertOrAbortMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) = with(appMetaData) {
        metaDataQueries.insertOrAbortMetaData(topic.value, name, description, url, icons, redirect?.native, appMetaDataType)
    }

    @Throws(SQLiteException::class)
    override fun updateMetaData(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) = with(appMetaData) {
        metaDataQueries.updateMetaData(name, description, url, icons, redirect?.native, appMetaDataType, topic.value)
    }

    @Throws(SQLiteException::class)
    override fun upsertPairingPeerMetadata(topic: Topic, appMetaData: AppMetaData, appMetaDataType: AppMetaDataType) {
        if (!existsByTopic(topic)) {
            insertOrAbortMetadata(topic, appMetaData, appMetaDataType)
        } else {
            updateMetaData(topic, appMetaData, appMetaDataType)
        }
    }

    override fun deleteMetaData(topic: Topic): Unit = metaDataQueries.deleteMetaDataFromTopic(topic.value)

    override fun existsByTopic(topic: Topic): Boolean = metaDataQueries.getIdByTopic(topic.value).executeAsOneOrNull() == null

    override fun getByTopic(topic: Topic): AppMetaData = metaDataQueries.getMetadataByTopic(topic.value, mapper = this::toMetadata).executeAsOne()

    private fun toMetadata(peerName: String, peerDesc: String, peerUrl: String, peerIcons: List<String>, native: String?, type: AppMetaDataType): AppMetaData =
        AppMetaData(peerName, peerDesc, peerUrl, peerIcons, Redirect(native = native))
}